package ast;

import ir.*;
import symboltable.*;
import temp.*;
import types.*;

public class AstDecFunc extends AstDec
{
	public AstType returnType;
    public String name;
    public AstFuncParamList params;
    public AstStmtList body;
    public static Type currentFunctionReturnType= null;

    public AstDecFunc(AstType returnType, String name, AstFuncParamList params, AstStmtList body, int lineNumber)
    {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.print("====================== dec -> varDec \n");

        this.returnType = returnType;
        this.name = name;
        this.params = params;
        this.body = body;
    }

    public void printMe()
    {
        System.out.print("AST NODE DEC FUNC\n");

        if (returnType != null) returnType.printMe();
        if (params != null) params.printMe();
        if (body != null) body.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "DEC\nFUNC\n" + name);

        if (returnType != null) AstGraphviz.getInstance().logEdge(serialNumber, returnType.serialNumber);
        if (params != null) AstGraphviz.getInstance().logEdge(serialNumber, params.serialNumber);
        if (body != null) AstGraphviz.getInstance().logEdge(serialNumber, body.serialNumber);
    }

    @Override
    public Type semantMe() {
        SymbolTable sym = SymbolTable.getInstance();

        Type ret = returnType.semantMe();
        if (ret == null) {
            error();
        }

        if (AstDecClass.currentClass == null) {
            if (sym.find(name) != null) {
                error();
            }
        } else {
            if (sym.findInCurrentScope(name) != null) {
                error();
            }

            TypeClass curAncestor = AstDecClass.currentClass.father;
            TypeFunction overridden = null;

            while (curAncestor != null && overridden == null) {
                TypeClassVarDecList members = curAncestor.dataMembers;
                while (members != null) {
                    TypeClassVarDec dec = members.head;
                    if (dec.t instanceof TypeFunction && dec.name.equals(name)) {
                        overridden = (TypeFunction) dec.t;
                        break;
                    }
                    members = members.tail;
                }
                curAncestor = curAncestor.father;
            }

            if (overridden != null) {
                TypeList thisParams = buildTypeList(params);

                TypeList p1 = overridden.params;
                TypeList p2 = thisParams;

                while (p1 != null && p2 != null) {
                    if (p1.head != p2.head) {
                        error();
                    }
                    p1 = p1.tail;
                    p2 = p2.tail;
                }

                if (p1 != null || p2 != null) {
                    error();
                }

                if (overridden.returnType != ret) {
                    error();
                }
            }
        }

        TypeList paramsTypes = buildTypeList(params);

        TypeFunction funcType = new TypeFunction(ret, name, paramsTypes);
        sym.enter(name, funcType, null, 0);

        if (AstDecClass.currentClass != null) {
            TypeClassVarDec newNode = new TypeClassVarDec(funcType, name);
            if (AstDecClass.currentClass.dataMembers == null) {
                AstDecClass.currentClass.dataMembers = new TypeClassVarDecList(newNode, null);
            } else {
                TypeClassVarDecList curr = AstDecClass.currentClass.dataMembers;
                while (curr.tail != null) {
                    curr = curr.tail;
                }
                curr.tail = new TypeClassVarDecList(newNode, null);
            }
        }

        sym.beginFunctionScope();

        if (AstDecClass.currentClass != null) {
            sym.enterParam("$this", AstDecClass.currentClass);
        }

        if (params != null) {
            sym.setVarKind(SymbolTable.VarKind.PARAM);
            params.semantMe();
            sym.clearVarKind();
        }

        currentFunctionReturnType = ret;

        if (body != null) {
            sym.setVarKind(SymbolTable.VarKind.LOCAL);
            body.semantMe();
            sym.clearVarKind();
        }

        currentFunctionReturnType = null;

        sym.endFunctionScope();

        return null;
    }

    private TypeList buildTypeList(AstFuncParamList l) {
        if (l == null) return null;
        Type t = l.head.type.semantMe();
        return new TypeList(t, buildTypeList(l.tail));
    }

    @Override
    public Temp irMe() {
        String emittedName = name;
        if (name.equals("main") && AstDecClass.currentClass == null) {
            emittedName = "user_main";
        }
        Ir.getInstance().currentFuncName = emittedName;
        
        Ir.getInstance().AddIrCommand(new IrCommandLabel("func_" + emittedName));
        
        // Allocate/load parameters
        if (params != null) {
            params.irMe();
        }
        
        // Emit function body (which handles its own returns)
        body.irMe();
        
        // Emit function end label
        Ir.getInstance().AddIrCommand(new IrCommandLabel("func_end_" + emittedName));
        
        Ir.getInstance().currentFuncName = null;
        return null;
    }
	
}
