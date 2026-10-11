package ir;

import mips.MipsGenerator;

public class IrCommandExit extends IrCommand
{
    @Override
    public void mipsMe()
    {
        MipsGenerator.getInstance().emitExit();
    }
}