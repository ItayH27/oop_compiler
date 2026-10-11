package ast;
import ir.*;
import symboltable.SymbolTable;
import temp.*;
import types.*;

public class AstProgram extends AstNode{
    public AstDec dec;
    public AstProgram next;

    public AstProgram(AstDec dec, AstProgram next, int lineNumber){
        super(lineNumber);

        serialNumber = AstNodeSerialNumber.getFresh();

        if (next != null) {
            System.out.print("====================== program -> dec program\n");
        } else {
            System.out.print("====================== program -> dec      \n");
        }

        this.dec = dec;
        this.next = next;
    }

    public void printMe(){
        System.out.print("AST PROGRAM\n");

        if (dec != null) dec.printMe();
        if (next != null) next.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "PROGRAM\n");

        if (dec != null) AstGraphviz.getInstance().logEdge(serialNumber, dec.serialNumber);
        if (next != null) AstGraphviz.getInstance().logEdge(serialNumber, next.serialNumber);
    }

    public Type semantMe() {
        SymbolTable table = SymbolTable.getInstance();

        if (dec != null) {
            dec.semantMe();
        }
        if (next != null) {
            next.semantMe();
        }
        return null;
    }

    public Temp irMe()
{
    // 1) allocate global storage
    for (AstProgram it = this; it != null; it = it.next)
    {
        if (it.dec instanceof AstDecVar)
        {
            ((AstDecVar) it.dec).irGlobalAllocate();
        }
    }

    // 2) emit class methods
    for (AstProgram it = this; it != null; it = it.next)
    {
        if (it.dec instanceof AstDecClass)
        {
            it.dec.irMe();
        }
    }

    // 3) emit global functions
    for (AstProgram it = this; it != null; it = it.next)
    {
        if (it.dec instanceof AstDecFunc)
        {
            it.dec.irMe();
        }
    }

    // 4) actual entry point
    Ir.getInstance().AddIrCommand(new IrCommandLabel("main"));

    // 5) initialize globals INSIDE main
    for (AstProgram it = this; it != null; it = it.next)
    {
        if (it.dec instanceof AstDecVar)
        {
            ((AstDecVar) it.dec).irGlobalInit();
        }
    }

    // 6) run user program
    Ir.getInstance().AddIrCommand(new IrCommandCall("user_main", java.util.List.of(), null));
    Ir.getInstance().AddIrCommand(new IrCommandExit());

    return null;
}
}