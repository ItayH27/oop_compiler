package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandNewArray extends IrCommand
{
    public final Temp dst;
    public final Temp len;

    public IrCommandNewArray(Temp dst, Temp len)
    {
        this.dst = dst;
        this.len = len;
    }

    @Override
    public String toString()
    {
        return dst + " = new_array " + len;
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().newArray(dst, len);
    }
}
