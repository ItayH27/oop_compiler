package ast;
import types.*;
import ir.*;
import symboltable.*;
import temp.Temp;

public class AstStmtWhile extends AstStmt
{
	public AstExp cond;
	public AstStmtList body;

	public AstStmtWhile(AstExp cond, AstStmtList body, int lineNumber)
	{
		super(lineNumber);

		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.print("====================== stmt -> WHILE LPAREN exp RPAREN LBRACE stmtList RBRACE\n");

		this.cond = cond;
		this.body = body;
	}

	public void printMe()
	{
		System.out.print("AST NODE WHILE STMT\n");

		if (cond != null) cond.printMe();
		if (body != null) body.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			"WHILE\nCOND BODY");

		AstGraphviz.getInstance().logEdge(serialNumber,cond.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,body.serialNumber);
	}
	public Type semantMe()
    {
        if (cond == null) { return null; }
        Type condType = cond.semantMe();
        if (condType == null) { return null; }

        if (condType != TypeInt.getInstance()) { 
			error(); 
			}
        SymbolTable.getInstance().beginScope();
        if (body != null) {
			 body.semantMe(); 
			 }
        SymbolTable.getInstance().endScope();
        return null;
    }

@Override
    public Temp irMe()
    {
        String labelStart = IrCommand.getFreshLabel("start");
        String labelEnd   = IrCommand.getFreshLabel("end");

        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelStart));

        Temp condTemp = cond.irMe();

        Ir.getInstance().AddIrCommand(new IrCommandJumpIfEqToZero(condTemp, labelEnd));

        if (body != null) {
            body.irMe();
        }

        Ir.getInstance().AddIrCommand(new IrCommandJumpLabel(labelStart));

        Ir.getInstance().AddIrCommand(new IrCommandLabel(labelEnd));

        return null;
    }
}