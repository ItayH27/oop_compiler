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

public class IrCommandLabel extends IrCommand
{
	public String labelName;
	
	public IrCommandLabel(String labelName)
	{
		this.labelName = labelName;
	}

	@Override
	public String toString() {
		return "LABEL " + labelName;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().label(labelName);
	}
}
