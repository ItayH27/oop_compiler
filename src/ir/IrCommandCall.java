package ir;

import java.util.List;
import mips.MipsGenerator;
import temp.*;

public class IrCommandCall extends IrCommand
{
    public String funcName;
    public List<Temp> args;
    public Temp dst;

    public IrCommandCall(String funcName,List<Temp> args, Temp dst)
    {
        this.funcName = funcName;
        this.args = args;
        this.dst = dst;
    }

    @Override
    public String toString()
    {
        return dst + " = call " + funcName + ", " + args;
    }

    @Override
    public void mipsMe()
    {
        if (funcName.equals("PrintInt")) {
            // expect exactly 1 arg
            MipsGenerator.getInstance().printInt(args.get(0));
            return;
        }
        if (funcName.equals("PrintString")) {
            MipsGenerator.getInstance().printString(args.get(0));
            return;
        }
        
        // push args in reverse order
        for (int i = args.size() - 1; i >= 0; i--) {
            MipsGenerator.getInstance().push(args.get(i));
        }

        MipsGenerator.getInstance().jal("func_" + funcName);

        // pop args
        MipsGenerator.getInstance().popBytes(args.size() * 4);

        if (dst != null) {
            MipsGenerator.getInstance().moveV0To(dst);
        }
    }
}