package ir;

import temp.*;
import mips.MipsGenerator;

public class IrCommandBinopEqStrings extends IrCommand
{
    public Temp dst, t1, t2;

    public IrCommandBinopEqStrings(Temp dst, Temp t1, Temp t2)
    {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public String toString()
    {
        return dst + " = eq " + t1 + ", " + t2;
    }

    public void mipsMe()
    {
        MipsGenerator.getInstance().strEq(dst, t1, t2);
    }
}
