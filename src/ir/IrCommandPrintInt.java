/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/

/*******************/
/* PROJECT IMPORTS */
/*******************/
import temp.*;
import mips.MipsGenerator;

public class IrCommandPrintInt extends IrCommand
{
	public Temp t;
	
	public IrCommandPrintInt(Temp t)
	{
		this.t = t;
	}

	@Override
	public String toString()
	{
		return "print_int " + t;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().printInt(t);
	}
}
