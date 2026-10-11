package ast;

import temp.*;

public abstract class AstNode
{
	/*******************************************/
	/* The serial number is for debug purposes */
	/* In particular, it can help in creating  */
	/* a graphviz dot format of the AST ...    */
	/*******************************************/
	public int serialNumber;
	public int lineNumber;
	
	public AstNode(int lineNumber) {
	    this.lineNumber = lineNumber;
	}
	/***********************************************/
	/* The default message for an unknown AST node */
	/***********************************************/
	public void printMe()
	{
		System.out.print("AST NODE UNKNOWN\n");
	}

	protected void error() {
		SemantError.error(lineNumber);
	}

	/*****************************************/
	/* The default IR action for an AST node */
	/*****************************************/
	public Temp irMe()
	{
		return null;
	}
}
