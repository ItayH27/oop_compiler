package ir;

import mips.MipsGenerator;
import temp.*;

public class IrCommandReturn extends IrCommand
{
    public Temp retVal;
    public String funcName;

    public IrCommandReturn(Temp retVal, String funcName)
    {
        this.retVal = retVal;
        this.funcName = funcName;

    }

    @Override
    public String toString()
    {
        return "return " + retVal + " ;\ngoto func_end_" + funcName;
    }

    @Override
    public void mipsMe()
    {
        if (retVal != null) {
            MipsGenerator.getInstance().moveToV0(retVal);
        }
        MipsGenerator.getInstance().jump("func_end_" + funcName);
    }
}