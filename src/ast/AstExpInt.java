package ast;
import ir.*;
import temp.*;
import types.*;

public class AstExpInt extends AstExp
{
	public int value;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpInt(int value, int lineNumber)
	{
		super(lineNumber);

		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== exp -> INT( %d )\n", value);

		this.value = value;
	}

	/************************************************/
	/* The printing message for an int exp AST node */
	/************************************************/
	public void printMe()
	{
		System.out.format("AST NODE INT( %d )\n",value);

		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("INT(%d)",value));
	}

	@Override
	public Type semantMe() {
		return TypeInt.getInstance();
	}

	public Temp irMe()
	{
		Temp dst = TempFactory.getInstance().getFreshTemp();
		Ir.getInstance().AddIrCommand(new IRcommandConstInt(dst, value));
		return dst;
	}

}
