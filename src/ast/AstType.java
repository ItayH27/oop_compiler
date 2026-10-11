package ast;
import types.*;
import symboltable.*;
import temp.Temp;


public class AstType extends AstNode{
    public String typeName;

    public AstType(String typeName, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("====================== type -> %s\n", typeName);

        this.typeName = typeName;
    }

    public void printMe() {
        System.out.print("AST NODE TYPE\n");

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "TYPE\n" + typeName);
    }
    
    public Type semantMe() {
        Type t = SymbolTable.getInstance().find(typeName);
        if (t == null) {
            error(); 
            return null;
        }
        return t;
    }
    public Temp irMe() {
        return null;
    }
}