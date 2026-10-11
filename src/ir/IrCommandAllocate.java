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
import mips.MipsGenerator;

public class IrCommandAllocate extends IrCommand
{
	public String varName;
	
	public IrCommandAllocate(String varName)
	{
		this.varName = varName;
	}

	@Override
	public String toString() {
		return "ALLOCATE " + varName;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().allocate(varName);
	}
}
