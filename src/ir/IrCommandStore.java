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

public class IrCommandStore extends IrCommand
{
	public String varName;
	public Temp src;
	
	public IrCommandStore(String varName, Temp src)
	{
		this.src      = src;
		this.varName = varName;
	}

	@Override
	public String toString() {
		return "store " + varName + " <- " + src;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().store(varName,src);
	}
}
