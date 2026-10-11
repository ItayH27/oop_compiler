package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandMalloc extends IrCommand
{
    public final Temp dst;
    public final int bytes;

    public IrCommandMalloc(Temp dst, int bytes)
    {
        this.dst = dst;
        this.bytes = bytes;
    }

    @Override
    public String toString()
    {
        return "MALLOC " + bytes + " -> " + dst;
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().malloc(dst, bytes);
    }
}
