package ir;

import temp.*;
import mips.MipsGenerator;

public class IrCommandBinopSubIntegers extends IrCommandBinop
{

	public IrCommandBinopSubIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public String toString()
	{
		return dst + " = sub " + t1 + ", " + t2;
	}
	public void mipsMe()
	{
		MipsGenerator.getInstance().sub(dst, t1, t2);
	}
}
