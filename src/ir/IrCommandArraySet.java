package ir;

import mips.MipsGenerator;
import temp.Temp;
import temp.TempFactory;

public class IrCommandArraySet extends IrCommand {
    public final Temp array;
    public final Temp index;
    public final Temp value;

    public IrCommandArraySet(Temp array, Temp index, Temp value) {
        this.array = array;
        this.index = index;
        this.value = value;
    }

    @Override
    public String toString() {
        return "array_set " + array + ", " + index + ", " + value;
    }

    @Override
    public void mipsMe() {
        MipsGenerator.getInstance().arraySet(array, index, value);
        // mips.MipsGenerator gen = mips.MipsGenerator.getInstance();

        // // temps
        // Temp zero = TempFactory.getInstance().getFreshTemp();
        // Temp one  = TempFactory.getInstance().getFreshTemp();
        // Temp four = TempFactory.getInstance().getFreshTemp();
        // Temp len  = TempFactory.getInstance().getFreshTemp();
        // Temp idx1 = TempFactory.getInstance().getFreshTemp();
        // Temp offB = TempFactory.getInstance().getFreshTemp();
        // Temp addr = TempFactory.getInstance().getFreshTemp();

        // gen.li(zero, 0);
        // gen.li(one, 1);
        // gen.li(four, 4);

        // gen.requireInvalidPtr();
        // gen.requireAccessViolation();

        // // null check
        // gen.beq(array, zero, "invalid_ptr_dref");

        // // bounds
        // gen.blt(index, zero, "access_violation");
        // gen.lw(len, array, 0);
        // gen.bge(index, len, "access_violation");

        // // addr = array + 4*(index+1)
        // gen.add(idx1, index, one);
        // gen.mul(offB, idx1, four);
        // gen.add(addr, array, offB);

        // // *addr = value
        // gen.sw(value, addr, 0);
    }
}
