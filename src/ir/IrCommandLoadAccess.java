package ir;

import temp.*;

public class IrCommandLoadAccess extends IrCommand {
    
    public Temp dst;    
    public Temp ref;    
    public int offset;  

    public IrCommandLoadAccess(Temp dst, Temp ref, int offset) {
        this.dst = dst;
        this.ref = ref;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "load_access " + dst.toString() + ", " + ref.toString() + ", " + offset;
    }
    
    @Override
    public void mipsMe() {
        mips.MipsGenerator.getInstance().loadAccess(dst, ref, offset);
    }
}