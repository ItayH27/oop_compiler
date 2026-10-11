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

public class IrCommandBinopLtIntegers extends IrCommandBinop
{

	public IrCommandBinopLtIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public String toString()
	{
		return dst + " = lt " + t1 + ", " + t2;
	}

	public void mipsMe()
	{
		/*******************************/
		/* [1] Allocate 2 fresh labels */
		/*******************************/
		String labelEnd        = getFreshLabel("end");
		String labelAssignOne  = getFreshLabel("AssignOne");
		String labelAssignZero = getFreshLabel("AssignZero");
		
		/******************************************/
		/* [2] if (t1< t2) goto labelAssignOne;  */
		/*     if (t1>=t2) goto labelAssignZero; */
		/******************************************/
		MipsGenerator.getInstance().blt(t1,t2,labelAssignOne);
		MipsGenerator.getInstance().bge(t1,t2,labelAssignZero);

		/************************/
		/* [3] labelAssignOne: */
		/*                      */
		/*         t3 := 1      */
		/*         goto end;    */
		/*                      */
		/************************/
		MipsGenerator.getInstance().label(labelAssignOne);
		MipsGenerator.getInstance().li(dst,1);
		MipsGenerator.getInstance().jump(labelEnd);

		/*************************/
		/* [4] labelAssignZero: */
		/*                       */
		/*         t3 := 0       */
		/*         goto end;     */
		/*                       */
		/*************************/
		MipsGenerator.getInstance().label(labelAssignZero);
		MipsGenerator.getInstance().li(dst,0);
		MipsGenerator.getInstance().jump(labelEnd);

		/******************/
		/* [5] labelEnd: */
		/******************/
		MipsGenerator.getInstance().label(labelEnd);
	}
}
