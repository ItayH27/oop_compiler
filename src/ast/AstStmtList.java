package ast;

import types.*;
import temp.*;

public class AstStmtList extends AstNode
{
	public AstStmt head;
	public AstStmtList tail;

	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstStmtList(AstStmt head, AstStmtList tail, int lineNumber)
	{
		super(lineNumber);
		serialNumber = AstNodeSerialNumber.getFresh();

		if (tail != null) System.out.print("====================== stmts -> stmt stmts\n");
		if (tail == null) System.out.print("====================== stmts -> stmt      \n");

		this.head = head;
		this.tail = tail;
	}

	/******************************************************/
	/* The printing message for a statement list AST node */
	/******************************************************/
	public void printMe()
	{
		System.out.print("AST NODE STMT LIST\n");

		if (head != null) head.printMe();
		if (tail != null) tail.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			"STMT\nLIST\n");

		if (head != null) AstGraphviz.getInstance().logEdge(serialNumber,head.serialNumber);
		if (tail != null) AstGraphviz.getInstance().logEdge(serialNumber,tail.serialNumber);
	}
	
	public Type semantMe() {
		if (head != null) {
			head.semantMe();
		}
		if (tail != null) {
			tail.semantMe();
		}
		return null;
	}

	public Temp irMe()
	{
		if (head != null) head.irMe();
		if (tail != null) tail.irMe();

		return null;
	}
}
