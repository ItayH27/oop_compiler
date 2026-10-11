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

public class IrCommandJumpIfEqToZero extends IrCommand
{
	public Temp t;
	public String labelName;
	
	public IrCommandJumpIfEqToZero(Temp t, String labelName)
	{
		this.t          = t;
		this.labelName = labelName;
	}

	@Override
	public String toString()
	{
		return "beq " + t + ", 0, " + labelName;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().beqz(t, labelName);
	}
}
