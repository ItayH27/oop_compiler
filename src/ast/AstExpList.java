package ast;
import java.util.*;
import temp.*;
import types.*;


public class AstExpList extends AstNode {
    public AstExp head;
    public AstExpList tail;

    public AstExpList(AstExp head, AstExpList tail, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        this.head = head;
        this.tail = tail;
        this.lineNumber = lineNumber;
    }

    public void printMe() {
        System.out.print("AST NODE EXP LIST\n");

        if (head != null) head.printMe();
        if (tail != null) tail.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "EXP\nLIST");

        if (head != null)
            AstGraphviz.getInstance().logEdge(serialNumber, head.serialNumber);
        if (tail != null)
            AstGraphviz.getInstance().logEdge(serialNumber, tail.serialNumber);
    }
    
    public TypeList semantMe() {
        Type headType = null;
        TypeList tailList = null;

        if (head != null)
            headType = head.semantMe();

        if (tail != null)
            tailList = tail.semantMe();

        return new TypeList(headType, tailList);
    }
	public List<Temp> irMeList()
{
    ArrayList<Temp> res = new ArrayList<>();
    AstExpList it = this;

    while (it != null)
    {
        res.add(it.head.irMe());
        it = it.tail;
    }
    return res;
}
}