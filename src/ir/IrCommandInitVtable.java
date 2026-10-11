package ir;

import mips.MipsGenerator;
import temp.Temp;

public class IrCommandInitVtable extends IrCommand {
    public Temp obj;
    public String className;

    public IrCommandInitVtable(Temp obj, String className) {
        this.obj = obj;
        this.className = className;
    }

    @Override
    public String toString() {
        return "init_vtable " + obj + ", vt_" + className;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().initVtable(obj, className);
    }
}