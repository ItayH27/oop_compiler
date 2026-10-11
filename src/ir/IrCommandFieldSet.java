package ir;

import temp.Temp;

public class IrCommandFieldSet extends IrCommand {
    public final Temp obj;
    public final int fieldOffset;
    public final Temp value;

    public IrCommandFieldSet(Temp obj, int fieldOffset, Temp value) {
        this.obj = obj;
        this.fieldOffset = fieldOffset;
        this.value = value;
    }

    @Override
    public String toString() {
        return "field_set " + obj + " " + fieldOffset + " " + value;
    }

    @Override
    public void mipsMe() {
        mips.MipsGenerator.getInstance().fieldSet(obj, 4 *( fieldOffset+1), value);
    }
}