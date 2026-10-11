package ast;
import ir.*;
import temp.*;
import types.*;

public class AstExpNew extends AstExp {
    public AstType type;
    public AstExp size; // null if not an array

    private TypeClass classType;

    public AstExpNew(AstType type, AstExp size, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        if (size == null) {
            System.out.format("====================== exp -> NEW type %s\n", type);
        } else {
            System.out.format("====================== exp -> NEW type %s [ exp ]\n", type);
        }
        this.type = type;
        this.size = size;
    }

    public void printMe() {
        System.out.print("AST NODE EXP NEW\n");

        if (size != null) {
            size.printMe();
        }
        type.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "EXP\nNEW\n" + type + (size != null ? " [ ]" : ""));

        AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
        if (size != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, size.serialNumber);
        }
    }

    
    public Type semantMe() {
        Type t = type.semantMe();

        if (size == null) {
            
            if (!(t instanceof TypeClass)) {
                error();
            }
            classType = (TypeClass) t;
            return t;
        }

        
        Type sizeType = size.semantMe();
        if (!(sizeType instanceof TypeInt)) {
            error();
        }

        Integer constSize = evalConstInt(size);
        if (constSize != null && constSize <= 0) {
            error();
        }

        if (t instanceof TypeVoid) {
            error();
        }

        return new TypeArray(null, t);
    }


    private Integer evalConstInt(AstExp e) {
        if (e == null) return null;

        if (e instanceof AstExpInt) {
            return ((AstExpInt) e).value;
        }

        if (e instanceof AstExpBinop) {
            AstExpBinop b = (AstExpBinop) e;

            Integer l = evalConstInt(b.left);
            Integer r = evalConstInt(b.right);
            if (l == null || r == null) return null;

            if (b.op == AstExpBinop.PLUS)   return l + r;
            if (b.op == AstExpBinop.MINUS)  return l - r;
            if (b.op == AstExpBinop.TIMES)  return l * r;
            if (b.op == AstExpBinop.DIVIDE) {
                if (r == 0) return null;
                return l / r;
            }

            return null;
        }

        return null;
    }
    private void emitFieldInitializers(Temp obj, TypeClass cls)
    {
        if (cls == null) return;

        // first initialize inherited fields
        emitFieldInitializers(obj, cls.father);

        AstDecClass classAst = AstDecClass.classAstByName.get(cls.name);
        if (classAst == null) return;

        for (AstCFieldList it = classAst.classFields; it != null; it = it.tail)
        {
            if (it.head instanceof AstDecVar)
            {
                AstDecVar v = (AstDecVar) it.head;

                if (v.initValue != null)
                {
                    int fieldOffset = cls.getFieldOffset(v.name);
                    if (fieldOffset < 0) {
                        error();
                    }

                    Temp value = v.initValue.irMe();
                    Ir.getInstance().AddIrCommand(
                        new IrCommandFieldSet(obj, fieldOffset, value)
                    );
                }
            }
        }
    }

    @Override
    public Temp irMe()
    {
        Temp dst = TempFactory.getInstance().getFreshTemp();

        // new T[e]  (array)
        if (size != null)
        {
            Temp len = size.irMe();
            Ir.getInstance().AddIrCommand(new IrCommandNewArray(dst, len));
            return dst;
        }

        // new ClassName  (object)
        int bytes = 4 * (classType.getTotalFieldCount() + 1); // +1 for vtable ptr
        Ir.getInstance().AddIrCommand(new IrCommandMalloc(dst, bytes));
        Ir.getInstance().AddIrCommand(new IrCommandInitVtable(dst, classType.name));

        // initialize fields with explicit initial values (including inherited ones)
        emitFieldInitializers(dst, classType);

        return dst;
    }

}

    


