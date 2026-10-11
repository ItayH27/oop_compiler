package ast;
import ir.*;
import temp.*;
import types.*;

public class AstFuncParamList extends AstNode {
    public AstDecVar head;
    public AstFuncParamList tail;

    public AstFuncParamList(AstDecVar head, AstFuncParamList tail, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("AST NODE FUNC PARAM LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "FUNC\nPARAM\nLIST");

        if (head != null)
            AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
        if (tail != null)
            AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
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
    @Override
   public Temp irMe() {
    // Allocate space for each parameter
    if (head != null) {
        Ir.getInstance().AddIrCommand(new ir.IrCommandAllocate(head.uniqueName));
        if (tail != null) {
            tail.irMe();
        }
    }
    return null;
}
}