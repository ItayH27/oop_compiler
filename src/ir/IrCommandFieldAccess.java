package ir;

import temp.Temp;

public class IrCommandFieldAccess extends IrCommand {
    public final Temp dst;
    public final Temp obj;
    public final int fieldOffset;

    public IrCommandFieldAccess(Temp dst, Temp obj, int fieldOffset) {
        this.dst = dst;
        this.obj = obj;
        this.fieldOffset = fieldOffset;
    }

    @Override
    public String toString() {
        return dst + " = field_access " + obj + " " + fieldOffset;
    }

    @Override
    public void mipsMe() {
        mips.MipsGenerator.getInstance().fieldAccess(dst, obj, 4 *( fieldOffset+1));
    }
}