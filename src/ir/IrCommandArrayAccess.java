package ir;

import temp.Temp;
import temp.TempFactory;
import mips.MipsGenerator;

public class IrCommandArrayAccess extends IrCommand {
    public final Temp dst;
    public final Temp array;
    public final Temp index;

    public IrCommandArrayAccess(Temp dst, Temp array, Temp index) {
        this.dst = dst;
        this.array = array;
        this.index = index;
    }

    @Override
    public String toString() {
        return dst + " = array_access " + array + ", " + index;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arrayAccess(dst, array, index);
        // MipsGenerator gen = MipsGenerator.getInstance();

        // // temps
        // Temp zero = TempFactory.getInstance().getFreshTemp();
        // Temp one  = TempFactory.getInstance().getFreshTemp();
        // Temp four = TempFactory.getInstance().getFreshTemp();
        // Temp len  = TempFactory.getInstance().getFreshTemp();
        // Temp idx1 = TempFactory.getInstance().getFreshTemp();
        // Temp offB = TempFactory.getInstance().getFreshTemp();
        // Temp addr = TempFactory.getInstance().getFreshTemp();

        // gen.requireInvalidPtr();
        // gen.requireAccessViolation();

        // gen.li(zero, 0);
        // gen.li(one, 1);
        // gen.li(four, 4);

        // // null check: if array == 0 -> invalid_ptr_dref
        // gen.beq(array, zero, "invalid_ptr_dref");

        // // bounds: if index < 0 -> access_violation
        // gen.blt(index, zero, "access_violation");

        // // len = array[0]
        // gen.lw(len, array, 0);

        // // if index >= len -> access_violation
        // gen.bge(index, len, "access_violation");

        // // addr = array + 4*(index+1)
        // gen.add(idx1, index, one);     // idx1 = index + 1
        // gen.mul(offB, idx1, four);     // offB = 4*(index+1)
        // gen.add(addr, array, offB);    // addr = array + offB

        // // dst = *addr
        // gen.lw(dst, addr, 0);
    }
}
