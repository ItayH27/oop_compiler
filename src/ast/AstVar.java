package ast;
import types.*;
import temp.*;

public abstract class AstVar extends AstNode
{
    public String uniqueName; 
    public AstVar(int lineNumber)
    {
        super(lineNumber);
    }
    public abstract Type semantMe();
    public abstract Temp irMe();
}
