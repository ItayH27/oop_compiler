/***********/
/* PACKAGE */
/***********/
package ir;

/*******************/
/* GENERAL IMPORTS */
/*******************/
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/*******************/
/* PROJECT IMPORTS */
/*******************/

public class Ir
{
	
	private IrCommand head=null;
	private IrCommandList tail=null;

	public String currentFuncName = null;

	/******************/
	/* Add Ir command */
	/******************/
	public void AddIrCommand(IrCommand cmd)
	{
		if ((head == null) && (tail == null))
		{
			this.head = cmd;
		}
		else if ((head != null) && (tail == null))
		{
			this.tail = new IrCommandList(cmd,null);
		}
		else
		{
			IrCommandList it = tail;
			while ((it != null) && (it.tail != null))
			{
				it = it.tail;
			}
			it.tail = new IrCommandList(cmd,null);
		}
	}

	/***************/
	/* MIPS me !!! */
	/***************/
	public void mipsMe()
	{
		if (head != null) head.mipsMe();
		if (tail != null) tail.mipsMe();
	}

	/**************************************/
	/* USUAL SINGLETON IMPLEMENTATION ... */
	/**************************************/
	private static Ir instance = null;

	/*****************************/
	/* PREVENT INSTANTIATION ... */
	/*****************************/
	protected Ir() {}

	/******************************/
	/* GET SINGLETON INSTANCE ... */
	/******************************/
	public static Ir getInstance()
	{
		if (instance == null)
		{
			/*******************************/
			/* [0] The instance itself ... */
			/*******************************/
			instance = new Ir();
		}
		return instance;
	}

	// Convert to list
	public List<IrCommand> toList()
    {
        List<IrCommand> out = new ArrayList<>();

        if (head != null) {
            out.add(head);
        }

        for (IrCommandList it = tail; it != null; it = it.tail) {
            if (it.head != null) {
                out.add(it.head);
            }
        }

        return out;
    }

	// Dump IR commands
	public void dump(PrintWriter out) {
        out.println("=== IR DUMP ===");
        if (head != null) dumpCmd(out, head);
        for (IrCommandList it = tail; it != null; it = it.tail) {
            if (it.head != null) dumpCmd(out, it.head);
        }
        out.println("=== END IR DUMP ===");
        out.flush();
    }

	// Dump single command
    private void dumpCmd(PrintWriter out, IrCommand cmd) {
        out.println(cmd);
    }

}
