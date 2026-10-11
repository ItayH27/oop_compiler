package ast;
import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstVarSubscript extends AstVar
{
	public AstVar var;
	public AstExp subscript;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSubscript(AstVar var, AstExp subscript, int lineNumber)
	{
		super(lineNumber);
		
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.print("====================== var -> var [ exp ]\n");

		this.var = var;
		this.subscript = subscript;
	}

	/*****************************************************/
	/* The printing message for a subscript var AST node */
	/*****************************************************/
	public void printMe()
	{

		System.out.print("AST NODE SUBSCRIPT VAR\n");

		if (var != null) var.printMe();
		if (subscript != null) subscript.printMe();
		
		AstGraphviz.getInstance().logNode(
				serialNumber,
			"SUBSCRIPT\nVAR\n...[...]");
		
		if (var       != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		if (subscript != null) AstGraphviz.getInstance().logEdge(serialNumber,subscript.serialNumber);
	}
	
	@Override
    public Type semantMe() {
        Type t = null;
        if (var != null) {
            t = var.semantMe();
        }
        if (t == null) return null; 

        if (t.isArray() == false) {
            error();
            return null;
        }
        Type subscriptType = null;
        
        if (subscript != null) {
            subscriptType = subscript.semantMe();
        }
        if (subscriptType == null) return null;

        if (!(subscriptType instanceof TypeInt)) {
			error();
			return null;
		}

		Integer idx = evalConstInt(subscript);
		if (idx != null && idx < 0) {
			error();
		}

        TypeArray arrayType = (TypeArray) t;
        return arrayType.elementType; 
    }

	private Integer evalConstInt(AstExp e) {
		if (e == null) return null;

		if (e instanceof AstExpInt) {
			return ((AstExpInt)e).value;
		}

		if (e instanceof AstExpBinop) {
			AstExpBinop b = (AstExpBinop)e;

			Integer l = evalConstInt(b.left);
			Integer r = evalConstInt(b.right);
			if (l == null || r == null) return null;

			if (b.op == AstExpBinop.PLUS)   return l + r;
			if (b.op == AstExpBinop.MINUS)  return l - r;
			if (b.op == AstExpBinop.TIMES)  return l * r;
			if (b.op == AstExpBinop.DIVIDE) {
				if (r == 0) return null; // don't fold; your binop already errors on /0 for literal
				return l / r;
			}

			return null;
		}

		return null;
	}

	public Temp irMe()
	{
		Temp arrayBase = var.irMe();
		Temp indexVal = subscript.irMe();
		
        Temp dst = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IrCommandArrayAccess(dst, arrayBase, indexVal));
		return dst;
	}
}