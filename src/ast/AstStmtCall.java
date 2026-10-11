package ast;

import temp.*;
import types.*;

public class AstStmtCall extends AstStmt
{
    public AstExpCall call;

    public AstStmtCall(AstExpCall call, int lineNumber)
    {
        super(lineNumber);

        serialNumber = AstNodeSerialNumber.getFresh();

        System.out.print("====================== stmt -> call SEMICOLON\n");

        this.call = call;
    }

    public void printMe()
    {
        System.out.print("AST NODE CALL STMT\n");

        if (call != null) call.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
            "CALL STMT");

        AstGraphviz.getInstance().logEdge(serialNumber,call.serialNumber);
    }

    public Type semantMe() {
        if (call != null) {
            call.semantMe();
        }
         
        return null;
    }

	public Temp irMe()
    {

            if (call != null) {
            call.irMe();
        }
            return null;
	}
}