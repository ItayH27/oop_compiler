package ast;
import symboltable.SymbolTable;
import types.*;
import temp.*;
import ir.*;

public class AstDecArray extends AstDec{
    public String name;
    public AstType type;

    public AstDecArray(String name, AstType type, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        System.out.format("====================== dec -> arrayTypedef %s\n", name);
        this.name = name;
        this.type = type;
    }

    public void printMe() {
        System.out.print("AST NODE DEC ARRAY\n");

        if (type != null) type.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "DEC\nARRAY\n" + name);

        if (type != null) AstGraphviz.getInstance().logEdge(serialNumber, type.serialNumber);
    }


    public Type semantMe() {
    
        Type elementType = type.semantMe();

        if (elementType == null) {
            error();
        }

        
        if (elementType instanceof TypeVoid) {
            error();
        }

        
        Type existing = SymbolTable.getInstance().find(name);
        if (existing != null) {
            error();
        }

        
        TypeArray arr = new TypeArray(name, elementType);
        SymbolTable.getInstance().enter(name, arr, null, 0);

        return null;
    }

    public Temp irMe() {
        return null;
    }

}
