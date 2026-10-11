package ast;

/*******************/
/* PROJECT IMPORTS */
/*******************/
import types.*;
import symboltable.*;
import temp.*;

public class AstTypeName extends AstNode
{
    /****************/
    /* DATA MEMBERS */
    /****************/
    public String type;
    public String name;
    
    /******************/
    /* CONSTRUCTOR(S) */
    /******************/
    public AstTypeName(String type, String name, int lineNumber)
    {

        super(lineNumber); 
        
        /******************************/
        /* SET A UNIQUE SERIAL NUMBER */
        /******************************/
        serialNumber = AstNodeSerialNumber.getFresh();
    
        this.type = type;
        this.name = name;
    }

    /*************************************************/
    /* The printing message for a type name AST node */
    /*************************************************/
    public void printMe()
    {
        System.out.format("NAME(%s):TYPE(%s)\n",name,type);

        AstGraphviz.getInstance().logNode(
                serialNumber,
            String.format("NAME:TYPE\n%s:%s",name,type));
    }

    public Type semantMe()
    {
        Type t = SymbolTable.getInstance().find(type);
        
        if (t == null)
        {
            SemantError.error(lineNumber);
            return null;
        }
        
        else
        {

            SymbolTable.getInstance().enter(name, t, null, 0);

            String uniqueName = name + "_" + lineNumber;
            SymbolTable.getInstance().bindVar(name, uniqueName);
        }

        return t;
    }   

    public Temp irMe()
    {
        return null;
    }
}