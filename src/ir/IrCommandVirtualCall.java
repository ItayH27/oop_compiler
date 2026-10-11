package ir;
import java.util.List;
import mips.MipsGenerator;
import temp.*;

public class IrCommandVirtualCall extends IrCommand {
    public Temp objTemp; // The object on which the method is called
    public String funcName;
    public int methodOffset; // The offset of the method in the vtable
    public List<Temp> args;
    public Temp dst;


    public IrCommandVirtualCall(Temp objTemp, String funcName,int methodOffset, List<Temp> args, Temp dst)
    {
        this.objTemp = objTemp;
        this.funcName = funcName;
        this.methodOffset = methodOffset;
        this.args = args;
        this.dst = dst;
    }

    @Override
    public String toString()
    {
        return dst + " = virtual call " + objTemp + ", " + funcName + "("+ methodOffset+"), " + args;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().virtualCall(objTemp, methodOffset, args, dst);
    }
}

    