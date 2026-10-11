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

public abstract class IrCommand
{
	/*****************/
	/* Label Factory */
	/*****************/
	protected static int labelCounter = 0;
	public    static String getFreshLabel(String msg)
	{
		return String.format("Label_%d_%s", labelCounter++,msg);
	}

	@Override
    public String toString() {
        return this.getClass().getSimpleName();
    }

	public abstract void mipsMe();
}
