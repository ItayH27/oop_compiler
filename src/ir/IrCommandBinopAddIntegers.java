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

public class IrCommandBinopAddIntegers extends IrCommandBinop
{
	
	public IrCommandBinopAddIntegers(Temp dst, Temp t1, Temp t2)
	{
		this.dst = dst;
		this.t1 = t1;
		this.t2 = t2;
	}

	@Override
	public String toString(){
		return dst + " = add " + t1 + ", " + t2;
	}

	public void mipsMe()
	{
		MipsGenerator.getInstance().add(dst,t1,t2);
	}
}
