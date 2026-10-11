package ast;
import types.*;
import temp.*;
import ir.*;

public abstract class AstStmt extends AstNode
{
    public AstStmt(int lineNumber)
    {
        super(lineNumber);
    }
    
    public abstract Type semantMe();

    public abstract Temp irMe();
}
