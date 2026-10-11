package ast;
import temp.*;
import types.*;

public class AstExpVar extends AstExp
{
	public AstVar var;


	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstExpVar(AstVar var, int lineNumber)
	{
		super(lineNumber);

		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.print("====================== exp -> var\n");

		this.var = var;
	}
	
	/***********************************************/
	/* The default message for an exp var AST node */
	/***********************************************/
	public void printMe()
	{
		System.out.print("AST NODE EXP VAR\n");

		if (var != null) var.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			"EXP\nVAR");

		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
			
	}

	@Override
	public Type semantMe() {
        
        return var.semantMe();
	}

    public Temp irMe() {
        return var.irMe();
    }
}
