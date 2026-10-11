package ast;
import types.*;
import symboltable.*;
import temp.*;
import ir.*;

public class AstExpNil extends AstExp{

    public AstExpNil(int lineNumber)
    {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("====================== exp -> NIL\n");
    }

    public void printMe()
    {
        System.out.format("AST NODE NIL\n");
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("NIL"));
    }
    @Override
    public Type semantMe() {
        return TypeNil.getInstance();
    }

    @Override
    public Temp irMe() 
    {
        Temp dst = TempFactory.getInstance().getFreshTemp();
        Ir.getInstance().AddIrCommand(new IRcommandConstInt(dst, 0));
        return dst;
    }
}