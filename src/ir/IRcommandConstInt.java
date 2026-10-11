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

public class IRcommandConstInt extends IrCommand
{
	public Temp t;
	public int value;
	
	public IRcommandConstInt(Temp t, int value)
	{
		this.t = t;
		this.value = value;
	}

	public String toString() {
		return "CONST " + value + " -> " + t;
	}

	@Override
	public void mipsMe()
	{
		MipsGenerator.getInstance().li(t, value);
	}
}
