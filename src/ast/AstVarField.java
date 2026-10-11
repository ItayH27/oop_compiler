package ast;
import ir.*;
import temp.*;
import types.*;

public class AstVarField extends AstVar
{
	public AstVar var;
	public String fieldName;
    public int fieldOffset;
	
	/******************/
	/* CONSTRUCTOR(S) */
	/******************/
	public AstVarField(AstVar var, String fieldName, int lineNumber)
	{
		super(lineNumber);
		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.format("====================== var -> var DOT ID( %s )\n",fieldName);

		this.var = var;
		this.fieldName = fieldName;
	}

	public void printMe()
	{
		System.out.print("AST NODE FIELD VAR\n");

		if (var != null) var.printMe();
		System.out.format("FIELD NAME( %s )\n",fieldName);

		AstGraphviz.getInstance().logNode(
				serialNumber,
			String.format("FIELD\nVAR\n...->%s",fieldName));

		if (var != null) AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
	}
	
	@Override
    public Type semantMe()  {
        Type t = null;
        if (var != null) {
            t = var.semantMe();
        }
        if (t == null) {
            return null;
        }
        if (t.isClass() == false) {
            error();
            return null;
        }
        TypeClass classType = (TypeClass) t;

        while (classType != null) {
            TypeClassVarDecList members = classType.dataMembers;           
            while (members != null) {
                if (members.head.name.equals(fieldName)) {
                    this.fieldOffset = members.head.offset;
                    return members.head.t; 
                }
                members = members.tail;
            }
            classType = classType.father;
        }
        error();
        return null;
    }

    public Temp irMe() {
        Temp obj = var.irMe();
        Temp dst = TempFactory.getInstance().getFreshTemp();

        Ir.getInstance().AddIrCommand(new IrCommandFieldAccess(dst, obj, fieldOffset));
        return dst;
    }

}