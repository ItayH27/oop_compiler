package ir;

import temp.*;
import mips.MipsGenerator;

public class IrCommandBinopConcatStrings extends IrCommand {
    public Temp dst, t1, t2;

    public IrCommandBinopConcatStrings(Temp dst, Temp t1, Temp t2)
    {
        this.dst = dst;
        this.t1 = t1;
        this.t2 = t2;
    }

    @Override
    public String toString() {
        return dst + " = concat " + t1 + ", " + t2;
    }

    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().emitConcatStrings();
        MipsGenerator.getInstance().concatStrings(dst, t1, t2);
    }
}
