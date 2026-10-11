package ast;
import ir.*;
import temp.*;
import types.*;

public class AstStmtReturn extends AstStmt {
    public AstExp retVal; // can be null

    public AstStmtReturn(AstExp retVal, int lineNumber) {
        super(lineNumber);

        serialNumber = AstNodeSerialNumber.getFresh();

        if (retVal == null) {
            System.out.print("====================== stmt -> RETURN SEMICOLON\n");
        } else {
            System.out.print("====================== stmt -> RETURN exp SEMICOLON\n");
        }

        this.retVal = retVal;
    }

    public void printMe() {
        System.out.print("AST NODE RETURN STMT\n");

        if (retVal != null) retVal.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "RETURN\nVALUE");

        if (retVal != null) {
            AstGraphviz.getInstance().logEdge(serialNumber, retVal.serialNumber);
        }
    
    }

    public Type semantMe() {
        Type expectedType = AstDecFunc.currentFunctionReturnType;
        if (expectedType == null) {
            error();
            return null;
        }

        // Case 1: function returns void
        if (expectedType == TypeVoid.getInstance()) {
            if (retVal != null) { // return exp; is illegal in void function
                error();
            }
            return null;
        }

        // Case 2: function returns non-void
        if (retVal == null) { // return; is illegal in non-void function
            error();
            return null;
        }

        Type actualType = retVal.semantMe();
        if (actualType == null) return null;

        // also reject "return <void expression>;" (e.g., return g(); where g returns void)
        if (actualType == TypeVoid.getInstance()) {
            error();
            return null;
        }

        // Now check compatibility: actual can be assigned to expected
        if (actualType == expectedType) return null;

        if (actualType == TypeNil.getInstance()) {
            if (expectedType.isClass() || expectedType.isArray()) return null;
            error();
            return null;
        }

        if (actualType.isClass() && expectedType.isClass()) {
            TypeClass child = (TypeClass) actualType;
            TypeClass parent = (TypeClass) expectedType;
            for (TypeClass c = child; c != null; c = c.father) {
                if (c == parent) return null;
            }
        }

        error();
        return null;
    }

	
	public Temp irMe()
    {
        Temp t = (retVal == null) ? null : retVal.irMe();
        String fn = Ir.getInstance().currentFuncName;
        Ir.getInstance().AddIrCommand(new IrCommandReturn(t, fn));
        return null;
    }
}
