package ir;

import mips.MipsGenerator;
import temp.*;

public class IrCommandPrintString extends IrCommand
{
    public Temp str;

    public IrCommandPrintString(Temp str)
    {
        this.str = str;
    }

    @Override
    public String toString()
    {
        return "print_string " + str;
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().printString(str);
    }
}
