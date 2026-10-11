package ast;
import ir.*;
import temp.*;
import types.*;

public class AstExpString extends AstExp{
    public String value;
    
    public AstExpString(String value, int lineNumber){
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("====================== exp -> String( %s )\n", value);
        this.value = value.substring(1,value.length()-1);
    }

    public void printMe(){
        System.out.format("AST NODE STRING( %s )\n",value);
        AstGraphviz.getInstance().logNode(
                serialNumber,
                String.format("STRING(%s)",value));
    }

    @Override
    public Type semantMe() {
        return TypeString.getInstance();
    }

    @Override
    public Temp irMe()
    {
        Temp dst = TempFactory.getInstance().getFreshTemp();

        // Create a unique label for this literal
        String label = IrCommand.getFreshLabel("str");

        // Emit data for the string literal (null-terminated in MIPS via .asciiz)
        Ir.getInstance().AddIrCommand(new IrCommandStringLiteral(label, value));

        // Load its address into a temp
        Ir.getInstance().AddIrCommand(new IrCommandLoadAddress(dst, label));

        return dst;
    }


}
