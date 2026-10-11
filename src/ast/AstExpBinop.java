package ast;
import ir.*;
import temp.*;
import types.*;

public class AstExpBinop extends AstExp
{
	/***************/
	/* OP CODES    */
	/***************/
	public static final int PLUS   = 0;
	public static final int MINUS  = 1;
	public static final int TIMES  = 2;
	public static final int DIVIDE = 3;
	public static final int LT     = 4;
	public static final int GT     = 5;
	public static final int EQ     = 6;
	
	int op;
	public AstExp left;
	public AstExp right;

	private Type leftType;
	private Type rightType;

	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpBinop(AstExp left, AstExp right, int op, int lineNumber)
	{
		super(lineNumber);
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.print("====================== exp -> exp BINOP exp\n");

		this.left = left;
		this.right = right;
		this.op = op;
	}
	
	/*************************************************/
	/* The printing message for a binop exp AST node */
	/*************************************************/
	public void printMe()
	{
		String sop="";
		
		if (op == PLUS)   sop = "+";
		if (op == MINUS)  sop = "-";
		if (op == TIMES)  sop = "*";
		if (op == DIVIDE) sop = "/";
		if (op == LT)     sop = "<";
		if (op == GT)     sop = ">";
		if (op == EQ)     sop = "=";

		System.out.print("AST NODE BINOP EXP\n");

		if (left != null) left.printMe();
		if (right != null) right.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("BINOP(%s)",sop));
		
		if (left  != null) AstGraphviz.getInstance().logEdge(serialNumber,left.serialNumber);
		if (right != null) AstGraphviz.getInstance().logEdge(serialNumber,right.serialNumber);
	}

	
	@Override
	public Type semantMe() {
		leftType = left.semantMe();
		rightType = right.semantMe();

		if (op == PLUS) {
			// int + int -> int
			if (leftType instanceof TypeInt && rightType instanceof TypeInt) {
				return TypeInt.getInstance();
			}

			// string + string -> string
			if (leftType instanceof TypeString && rightType instanceof TypeString) {
				return TypeString.getInstance();
			}

			// anything else is illegal
			error();
		}

		if (op == MINUS || op == TIMES || op == DIVIDE) {
			if (!(leftType instanceof TypeInt) || !(rightType instanceof TypeInt)) {
				error();
			}

			// extra semantic check: division by zero in "x / 0"
			if (op == DIVIDE && right instanceof AstExpInt) {
				AstExpInt r = (AstExpInt) right;
				if (r.value == 0) {
					error();
				}
			}

			return TypeInt.getInstance();
		}

		if (op == LT || op == GT) {
			if (!(leftType instanceof TypeInt) || !(rightType instanceof TypeInt)) {
				error();
			}
			return TypeInt.getInstance();
		}

		if (op == EQ) {
			if (leftType instanceof TypeNil && (rightType instanceof TypeClass || rightType instanceof TypeArray)) {
				return TypeInt.getInstance();
			}
			if (rightType instanceof TypeNil && (leftType instanceof TypeClass || leftType instanceof TypeArray)) {
				return TypeInt.getInstance();
			}

			// int=int or string=string
			if ((leftType instanceof TypeInt && rightType instanceof TypeInt) ||
				(leftType instanceof TypeString && rightType instanceof TypeString)) {
				return TypeInt.getInstance();
			}

			// arrays: ONLY same typedef (nominal)
			if (leftType instanceof TypeArray && rightType instanceof TypeArray) {
				if (leftType == rightType) { // same named array type object
					return TypeInt.getInstance();
				}
				error();
				return null;
			}

			// classes: allow if same class OR one is subclass of the other
			if (leftType instanceof TypeClass && rightType instanceof TypeClass) {
				if (TypeUtils.isAssignable(leftType, rightType) || TypeUtils.isAssignable(rightType, leftType)) {
					return TypeInt.getInstance();
				}
				error();
				return null;
			}

			error();
		}

		error();
		return null;
	}
	
	@Override
public Temp irMe()
{
    Temp t1 = left.irMe();
    Temp t2 = right.irMe();
    Temp dst = TempFactory.getInstance().getFreshTemp();

    // We need the semantic types to decide which IR to emit (int vs string vs array/class)
    Type ty1 = leftType;
    Type ty2 = rightType;

    switch (op)
    {
        case PLUS:
            // int + int  (with saturation in MIPS for AddIntegers)
            if (ty1 instanceof TypeInt && ty2 instanceof TypeInt) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopAddIntegers(dst, t1, t2));
                return dst;
            }

            // string + string  (concat)
            if (ty1 instanceof TypeString && ty2 instanceof TypeString) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopConcatStrings(dst, t1, t2));
                return dst;
            }

            throw new RuntimeException("Illegal PLUS types");

        case MINUS:
            Ir.getInstance().AddIrCommand(new IrCommandBinopSubIntegers(dst, t1, t2));
            return dst;

        case TIMES:
            Ir.getInstance().AddIrCommand(new IrCommandBinopMulIntegers(dst, t1, t2));
            return dst;

        case DIVIDE:
            Ir.getInstance().AddIrCommand(new IrCommandBinopDivIntegers(dst, t1, t2));
            return dst;

        case LT:
            Ir.getInstance().AddIrCommand(new IrCommandBinopLtIntegers(dst, t1, t2));
            return dst;

        case GT:
            Ir.getInstance().AddIrCommand(new IrCommandBinopGtIntegers(dst, t1, t2));
            return dst;

        case EQ:
            // string = string  (content equality)
            if (ty1 instanceof TypeString && ty2 instanceof TypeString) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqStrings(dst, t1, t2));
                return dst;
            }

            // array = array  (address equality)
            if (ty1 instanceof TypeArray && ty2 instanceof TypeArray) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
                return dst;
            }

            // class = class  (address equality)
            if (ty1 instanceof TypeClass && ty2 instanceof TypeClass) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
                return dst;
            }

            // nil comparisons allowed by semantMe (nil vs class/array) -> address equality is fine
            if (ty1 instanceof TypeNil && (ty2 instanceof TypeClass || ty2 instanceof TypeArray)) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
                return dst;
            }
            if (ty2 instanceof TypeNil && (ty1 instanceof TypeClass || ty1 instanceof TypeArray)) {
                Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
                return dst;
            }

            // int = int (and any other “address-like” equality you allow) -> integer compare
            Ir.getInstance().AddIrCommand(new IrCommandBinopEqIntegers(dst, t1, t2));
            return dst;

        default:
            throw new RuntimeException("Unsupported binop op=" + op);
    }
}


}
