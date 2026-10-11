package ast;

import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstStmtIf extends AstStmt
{
	public AstExp cond;
	public AstStmtList then_body;
	public AstStmtList else_body; // can be null

	public AstStmtIf(AstExp cond, AstStmtList then_body, AstStmtList else_body, int lineNumber)
	{
		super(lineNumber);

		serialNumber = AstNodeSerialNumber.getFresh();

		if (else_body == null) {
			System.out.print("====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE\n");
		} else {
			System.out.print("====================== stmt -> IF LPAREN exp RPAREN LBRACE stmtList RBRACE ELSE LBRACE stmtList RBRACE\n");
		}

		this.cond = cond;
		this.then_body = then_body;
		this.else_body = else_body;
	}

	public void printMe()
	{
		System.out.print("AST NODE IF STMT\n");

		if (cond != null) cond.printMe();
		if (then_body != null) then_body.printMe();
		if (else_body != null) else_body.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			"IF\nCOND THEN ELSE");

		AstGraphviz.getInstance().logEdge(serialNumber,cond.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,then_body.serialNumber);
		if (else_body != null) {
			AstGraphviz.getInstance().logEdge(serialNumber,else_body.serialNumber);
		}
	}

	public Type semantMe() {
		Type condType = cond.semantMe();
		if (condType != TypeInt.getInstance()) {
			error();
		}

		SymbolTable.getInstance().beginScope();
		then_body.semantMe();
		SymbolTable.getInstance().endScope();

		if (else_body != null) {
			SymbolTable.getInstance().beginScope();
			else_body.semantMe();
			SymbolTable.getInstance().endScope();
		}

		return null;		
	}

	public Temp irMe()
	{
		// if (cond) { then_body } else { else_body }
		Temp condTemp = cond.irMe(); // cond result

		// create labels
		String falseLabel = IrCommand.getFreshLabel("false_label");
		String endLabel = IrCommand.getFreshLabel("end_label");

		Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, falseLabel)); // beq condTemp, $zero, falseLabel

		Temp thenTemp = then_body.irMe(); // code for then body

		Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(endLabel)); // br endLabel

		Ir.getInstance().AddIrCommand(new IrCommandLabel(falseLabel)); // falseLabel:

		if (else_body != null) {
			Temp elseTemp = else_body.irMe(); // code for else body
		}

		Ir.getInstance().AddIrCommand(new IrCommandLabel(endLabel)); // endLabel:

		return null;
	}
}
