package ast;

import ir.*;
import temp.*;
import types.*;

public class AstStmtAssign extends AstStmt
{
	public AstVar var;
	public AstExp exp;

	/*******************/
	/*  CONSTRUCTOR(S) */
	/*******************/
	public AstStmtAssign(AstVar var, AstExp exp, int lineNumber)
	{
		super(lineNumber);

		serialNumber = AstNodeSerialNumber.getFresh();

		System.out.print("====================== stmt -> var ASSIGN exp SEMICOLON\n");

		this.var = var;
		this.exp = exp;
	}

	public void printMe()
	{
		System.out.print("AST NODE ASSIGN STMT\n");

		if (var != null) var.printMe();
		if (exp != null) exp.printMe();

		AstGraphviz.getInstance().logNode(
				serialNumber,
			"ASSIGN\nleft := right\n");

		AstGraphviz.getInstance().logEdge(serialNumber,var.serialNumber);
		AstGraphviz.getInstance().logEdge(serialNumber,exp.serialNumber);
	}

	public Type semantMe() {
		Type tleft = null;
		Type tright = null;

		if (var != null) {
			tleft = var.semantMe();
		}
		if (exp != null) {
			tright = exp.semantMe();
		}

		if (!TypeUtils.isAssignable(tleft, tright)) {
			error();
		}
		return null;
	}

	public Temp irMe()
	{
		// x := exp
		if (var instanceof AstVarSimple) {
			Temp src = exp.irMe();

			if (var instanceof AstVarSimple vs && vs.fieldOffset >= 0) {
				Temp thisObj = TempFactory.getInstance().getFreshTemp();
				Ir.getInstance().AddIrCommand(new IrCommandLoad(thisObj, "param_1"));
				Ir.getInstance().AddIrCommand(new IrCommandFieldSet(thisObj, vs.fieldOffset, src));
			} else {
				Ir.getInstance().AddIrCommand(new IrCommandStore(var.uniqueName, src));
			}
			return null;
		}

		// a[i] := exp
		if (var instanceof AstVarSubscript) {
			AstVarSubscript vs = (AstVarSubscript) var;
			Temp arr = vs.var.irMe();
			Temp idx = vs.subscript.irMe();
			Temp src = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandArraySet(arr, idx, src));
			return null;
		}

		// obj.f := exp
		if (var instanceof AstVarField) {
			AstVarField vf = (AstVarField) var;
			Temp obj = vf.var.irMe();
			Temp src = exp.irMe();
			Ir.getInstance().AddIrCommand(new IrCommandFieldSet(obj, vf.fieldOffset, src));
			return null;
		}

		return null;
	}
}
