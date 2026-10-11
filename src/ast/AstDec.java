package ast;

import types.*;

public abstract class AstDec extends AstStmt
{
	/***********************************************/
	/* The default semantic action for an AST node */
	/***********************************************/
	public AstDec(int lineNumber) {
        super(lineNumber);
    }
	public abstract Type semantMe();
}
