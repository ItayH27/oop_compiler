package ast;
import ir.*;
import java.util.ArrayList;
import java.util.List;
import symboltable.SymbolTable;
import temp.*;
import types.*;


public class AstExpCall extends AstExp{
    public String funcName;
    public AstVar className; // null if no class
    public AstExpList args;
    private int methodOffset = -1; 
    private boolean isImplicitMethodCall = false;
    private TypeClass ownerClassForImplicitCall = null;

    public AstExpCall(AstVar className, String funcName, AstExpList args, int lineNumber){
        super(lineNumber);

        serialNumber = AstNodeSerialNumber.getFresh();

        if (className != null) {
            System.out.print("====================== exp -> var.ID( expList )\n");
        } else {
            System.out.print("====================== exp -> ID( expList )\n");
        }

        this.funcName = funcName;
        this.className = className;
        this.args = args;
    }

    public void printMe()
    {

        System.out.print("AST NODE EXP CALL\n");

        if (className != null) className.printMe();

        if (args != null) args.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "EXP\nCALL\n" + funcName);

        if (className != null) AstGraphviz.getInstance().logEdge(serialNumber, className.serialNumber);
        if (args != null) AstGraphviz.getInstance().logEdge(serialNumber, args.serialNumber);
    }

    
    public Type semantMe() {

        TypeFunction funcType = null;

        // Case 1: obj.f(...)
        if (className != null) {
            Type t = className.semantMe();
            if (!(t instanceof TypeClass)) {
               error();
            }

            TypeClass cls = (TypeClass) t;

            // lookup method in class
            funcType = findMethodInClass(cls, funcName);
            if (funcType == null) {
               error();
            }

            methodOffset = cls.getMethodOffset(funcName);
            if (methodOffset < 0) {
                error();
            }
        }
        else {
             // If we are inside a class, prefer resolving as a method of "this"
            if (AstDecClass.currentClass != null) {
                funcType = findMethodInClass(AstDecClass.currentClass, funcName);
                if (funcType != null) {
                    isImplicitMethodCall = true;
                    ownerClassForImplicitCall = AstDecClass.currentClass;
                    methodOffset = AstDecClass.currentClass.getMethodOffset(funcName);
                }
            }

            // If not found as method, resolve as a global function
            if (funcType == null) {
                Type t = SymbolTable.getInstance().find(funcName);
                if (!(t instanceof TypeFunction)) {
                    error();
                }
                funcType = (TypeFunction) t;
            }
        }

        // Semant arguments
        TypeList givenArgs = null;
        if (args != null)
            givenArgs =  args.semantMe();

        // Compare lists
        checkParamCompatibility(funcType.params, givenArgs);

        return funcType.returnType;
    }

   private TypeFunction findMethodInClass(TypeClass cls, String name) {
    for (TypeClass c = cls; c != null; c = c.father) {
        TypeClassVarDecList members = c.dataMembers;
        while (members != null) {
            TypeClassVarDec dec = members.head;
            if (dec.t instanceof TypeFunction && dec.name.equals(name)) {
                return (TypeFunction) dec.t;
            }
            members = members.tail;
        }
    }
    return null;
}

    private void checkParamCompatibility(TypeList params, TypeList args) {
        TypeList p = params;
        TypeList a = args;

        while (p != null && a != null) {

            if (!TypeUtils.isAssignable(p.head, a.head)) {
                error();
            }

            p = p.tail;
            a = a.tail;
        }

        if (p != null || a != null) {
            error();
        }
    }

	public Temp irMe()
    {
        // Builtins: PrintInt / PrintString
        if ("PrintInt".equals(funcName) || "PrintString".equals(funcName))
        {
            // exactly one argument
            if (args == null || args.head == null || args.tail != null)
                throw new RuntimeException(funcName + " expects exactly one argument");

            Temp arg = args.head.irMe();

            if ("PrintInt".equals(funcName))
            {
                Ir.getInstance().AddIrCommand(new IrCommandPrintInt(arg));
                return null;
            }

            // PrintString
            Ir.getInstance().AddIrCommand(new IrCommandPrintString(arg));
            return null;
        }

        List<Temp> argTemps = new ArrayList<>();
        for (AstExpList it = args; it != null; it = it.tail) {
            argTemps.add(it.head.irMe());   // left-to-right evaluation
        }

       Temp dst = TempFactory.getInstance().getFreshTemp();

        if (className != null) {
            Temp objTemp = className.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandVirtualCall(objTemp, funcName, methodOffset, argTemps, dst));
        } else if (isImplicitMethodCall) {
            Temp thisObj = TempFactory.getInstance().getFreshTemp();
            Ir.getInstance().AddIrCommand(new IrCommandLoad(thisObj, "param_1")); // hidden this
            Ir.getInstance().AddIrCommand(new IrCommandVirtualCall(thisObj, funcName, methodOffset, argTemps, dst));
        } else {
            Ir.getInstance().AddIrCommand(new IrCommandCall(funcName, argTemps, dst));
        }

    return dst;
    }
}