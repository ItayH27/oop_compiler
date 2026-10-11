package ast;
import ir.*;
import symboltable.*;
import temp.*;
import types.*;

public class AstVarSimple extends AstVar
{
	public String name;
	public int fieldOffset = -1;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarSimple(String name, int lineNumber)
	{
		super(lineNumber);
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== var -> ID( %s )\n",name);

		this.name = name;
	}

	/**************************************************/
	/* The printing message for a simple var AST node */
	/**************************************************/
	public void printMe()
	{

		System.out.format("AST NODE SIMPLE VAR( %s )\n",name);

		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("SIMPLE\nVAR\n(%s)",name));
	}

	@Override
	public Type semantMe() {
		String id = this.name;
		SymbolTable sym = SymbolTable.getInstance();

		String resolved = sym.resolveVar(id);
		if (resolved != null) {
			this.uniqueName = resolved;

			Type t = sym.find(id);
			if (t == null) error();

			if (this.uniqueName.startsWith("field_")) {
				this.fieldOffset = AstDecClass.currentClass.getFieldOffset(id);
			}

			return t;
		}

		// fallback: implicit this.field, including inherited fields
		if (AstDecClass.currentClass != null) {
			int off = AstDecClass.currentClass.getFieldOffset(id);
			if (off >= 0) {
				TypeClass c = AstDecClass.currentClass;
				while (c != null) {
					for (TypeClassVarDecList it = c.dataMembers; it != null; it = it.tail) {
						if (!(it.head.t instanceof TypeFunction) && it.head.name.equals(id)) {
							this.fieldOffset = off;
							this.uniqueName = "field_implicit";
							return it.head.t;
						}
					}
					c = c.father;
				}
			}
		}

		error();
		return null;
	}

   public Temp irMe() {
		Temp dst = TempFactory.getInstance().getFreshTemp();

		// implicit this.field
		if (this.fieldOffset >= 0) {
			Temp thisObj = TempFactory.getInstance().getFreshTemp();
			Ir.getInstance().AddIrCommand(new IrCommandLoad(thisObj, "param_1"));
			Ir.getInstance().AddIrCommand(new IrCommandFieldAccess(dst, thisObj, fieldOffset));
			return dst;
		}

		Ir.getInstance().AddIrCommand(new IrCommandLoad(dst, this.uniqueName));
		return dst;
	}
}