/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;
import mips.MipsGenerator;

public class IrCommandLoad extends IrCommand
{
	public Temp dst;
	public String varName;
	
	public IrCommandLoad(Temp dst, String varName)
	{
		this.dst      = dst;
		this.varName = varName;
	}

	@Override
	public String toString()
	{
		return "load " + dst + " <- " + varName;
	}
	
	public void mipsMe()
	{
		MipsGenerator.getInstance().load(dst, varName);
	}
}
