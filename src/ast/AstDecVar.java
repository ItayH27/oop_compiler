package ast;
import ir.*;
import symboltable.SymbolTable;
import symboltable.SymbolTableEntry;
import temp.*;
import types.*;

public class AstDecVar extends AstDec
{
    public AstType type;
    public String name;
    public AstExp initValue;
	public String uniqueName;

    public AstDecVar(AstType type, String name, AstExp initValue, int lineNumber)
    {
        super(lineNumber);

        serialNumber = AstNodeSerialNumber.getFresh();

        if (initValue == null){
            System.out.print("====================== dec -> type ID ;\n");
        } else {
            System.out.print("====================== dec -> type ID ASSIGN exp ;\n");
        }

        this.type = type;
        this.name = name;
        this.initValue = initValue;
    }

    public void printMe()
    {
        System.out.print("AST NODE DEC VAR\n");

        if (type != null) type.printMe();
        if (initValue != null) initValue.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "DEC\nVAR\n" + name);

        if (type != null) AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (initValue != null) AstGraphviz.getInstance().logEdge(serialNumber, initValue.serialNumber);
    }

    @Override
    public Type semantMe() {

        SymbolTable sym = SymbolTable.getInstance();

        // 1) type checks
        Type t = type.semantMe();
        if (t == null) error();
        if (t instanceof TypeVoid) error();

        // 2) duplicate in current scope
        Type existing = sym.findInCurrentScope(name);
        if (existing != null) error();

        // 3) class-field shadows superclass method (your existing check)
        if (AstDecClass.currentClass != null) {
            for (TypeClass c = AstDecClass.currentClass.father; c != null; c = c.father) {
                for (TypeClassVarDecList m = c.dataMembers; m != null; m = m.tail) {
                    TypeClassVarDec dec = m.head;
                    if (dec.name.equals(name) && dec.t instanceof TypeFunction) {
                        error();
                    }
                }
            }
        }

        // 4) ENTER THE VAR FIRST (this assigns varKind + index in SymbolTableEntry)
        sym.enterVarByMode(name, t);

        // 5) build uniqueName based on the entry (param/local/field), else global
        SymbolTableEntry e = sym.findEntry(name);   // or sym.findEntryInCurrentScope(name) if you have it
        if (e != null && e.varKind != null) {
            // IMPORTANT: you need SymbolTableEntry to expose something like offsetLabel
            // offsetLabel should be: "param_1" / "local_1" / "field_1"
            uniqueName = e.offsetLabel;
        } else {
            uniqueName = name; // global variable
        }

        // 6) bind source name -> IR name (so AstVarSimple uses the right name)
        sym.bindVar(name, uniqueName);

        // 7) init value checks (your existing logic)
        if (initValue != null) {
            Type initT = initValue.semantMe();

            if (initT instanceof TypeNil) {
                if (!(t instanceof TypeClass || t instanceof TypeArray)) {
                    error();
                }
            } else if (!TypeUtils.isAssignable(t, initT)) {
                error();
            }
        }

        // 8) if it is a class field, add to dataMembers (keep your existing behavior)
        if (AstDecClass.currentClass != null && e != null && e.varKind == SymbolTableEntry.VarKind.FIELD) {
            TypeClassVarDec newNode = new TypeClassVarDec(t, name);
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

        return null;
    }
		// uniqueName = name + "_" + lineNumber;
		// SymbolTable.getInstance().bindVar(name, uniqueName);
    
        // Type t = type.semantMe();
        // if (t == null) {
        //     error();
        // }
        
        // if (t instanceof TypeVoid) {
        //     error();
        // }
        
        // Type existing = SymbolTable.getInstance().findInCurrentScope(name);
        // if (existing != null) {
        //     error();
        // }

        // if (AstDecClass.currentClass != null) {
        //     for (TypeClass c = AstDecClass.currentClass.father; c != null; c = c.father) {
        //         for (TypeClassVarDecList m = c.dataMembers; m != null; m = m.tail) {
        //             TypeClassVarDec dec = m.head;
        //             if (dec.name.equals(name) && dec.t instanceof TypeFunction) {
        //                 error(); // field shadows superclass method
        //             }
        //         }
        //     }
        // }
        
        // if (initValue == null) {
        //     SymbolTable.getInstance().enterVarByMode(name, t);
        //     if (AstDecClass.currentClass != null) {
        //         AstDecClass.currentClass.dataMembers =
        //             new TypeClassVarDecList(new TypeClassVarDec(t, name),
        //                                     AstDecClass.currentClass.dataMembers);
        //     }
        //     return null;
        // }
        
        // Type initT = initValue.semantMe();
        
        // if (initT instanceof TypeNil) {
        //     if (!(t instanceof TypeClass || t instanceof TypeArray)) {
        //     error();
        //     }
        // }
        
        // else if (!TypeUtils.isAssignable(t, initT)) {
        //     error();
        // }

        // SymbolTable.getInstance().enterVarByMode(name, t);
        // return null;

    public Temp irGlobalAllocate()
    {
        Ir.getInstance().AddIrCommand(new IrCommandAllocate(uniqueName));
        return null;
    }

    public Temp irGlobalInit()
    {
        if (initValue != null)
        {
            Temp rhs = initValue.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandStore(uniqueName, rhs));
        }
        return null;
    }

	public Temp irMe()
	{
		Ir.getInstance().AddIrCommand(new IrCommandAllocate(uniqueName));

		if (initValue != null)
		{
			Temp rhs = initValue.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandStore(uniqueName, rhs));
		}
		return null;
	}

}

    