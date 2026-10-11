package ir;

import temp.Temp;
import mips.MipsGenerator;

public class IrCommandLoadAddress extends IrCommand
{
    public final Temp dst;
    public final String label;

    public IrCommandLoadAddress(Temp dst, String label)
    {
        this.dst = dst;
        this.label = label;
    }

    @Override
    public String toString()
    {
        return "load_address " + dst + " <- " + label;
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().la(dst, label);
    }
}
