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

public class IrCommandList
{
	public IrCommand head;
	public IrCommandList tail;

	public IrCommandList(IrCommand head, IrCommandList tail)
	{
		this.head = head;
		this.tail = tail;
	}

	public void mipsMe()
	{
		if (head != null) head.mipsMe();
		if (tail != null) tail.mipsMe();
	}
}
