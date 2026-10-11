package ast;
import types.*; 
import ir.*;
import temp.*;

public class AstCFieldList extends AstNode {
    public AstDec head;
    public AstCFieldList tail;

    public AstCFieldList(AstDec head, AstCFieldList tail, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("AST NODE C FIELD LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "C\nFIELD\nLIST");

        if (head != null)
            AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
        if (tail != null)
            AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
    }

    
    public Type semantMe() {
        if (head != null) head.semantMe();
        if (tail != null) tail.semantMe();
        return null;
    }

    public Temp irMe()
    {

        // emit IR for methods only
        for (AstCFieldList it = this; it != null; it = it.tail)
        {
            if (it.head instanceof AstDecFunc)
            {
                it.head.irMe();
            }
        }

        return null;
    }

}