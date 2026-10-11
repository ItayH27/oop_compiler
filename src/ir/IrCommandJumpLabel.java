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

public class IrCommandJumpLabel extends IrCommand
{
	public String labelName;
	
	public IrCommandJumpLabel(String labelName)
	{
		this.labelName = labelName;
	}

	@Override
	public String toString()
	{
		return "br " + labelName;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().jump(labelName);
	}
}
