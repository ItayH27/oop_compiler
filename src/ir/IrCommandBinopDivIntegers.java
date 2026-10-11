package ir;

import temp.*;
import mips.MipsGenerator;

public class IrCommandBinopDivIntegers extends IrCommandBinop
{
	
	public IrCommandBinopDivIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public String toString()
	{
		return dst + " = div " + t1 + ", " + t2;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().div(dst, t1, t2);
	}
}
