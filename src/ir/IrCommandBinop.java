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

public abstract class IrCommandBinop extends IrCommand
{
    public Temp dst;
    public Temp t1;
    public Temp t2;
}