package ast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import symboltable.SymbolTable;
import temp.*;
import types.*;


public class AstDecClass extends AstDec{
    public String name;
    public String superClassName; // null if no extends
    public AstCFieldList classFields;
    public static TypeClass currentClass = null;
    public static List<TypeClass> allClasses = new ArrayList<>();
    public static Map<String, AstDecClass> classAstByName = new HashMap<>();

    public AstDecClass(String name, String superClassName, AstCFieldList classFields, int lineNumber) {
        super(lineNumber);
        serialNumber = AstNodeSerialNumber.getFresh();
        if (superClassName != null){
            System.out.format("====================== dec -> classDec %s EXTENDS %s\n", name, superClassName);
        } else {
            System.out.format("====================== dec -> classDec %s\n", name);
        }
        this.name = name;
        this.superClassName = superClassName;
        this.classFields = classFields;
    }
    
    public void printMe() {
        System.out.print("AST NODE DEC CLASS\n");

        if (classFields != null) classFields.printMe();

        AstGraphviz.getInstance().logNode(
                serialNumber,
                "DEC\nCLASS\n" + name + (superClassName != null ? (" EXTENDS " + superClassName) : ""));

        if (classFields != null) AstGraphviz.getInstance().logEdge(serialNumber, classFields.serialNumber);
    }

    @Override
    public Type semantMe() {
        SymbolTable sym = SymbolTable.getInstance();

        if (sym.find(name) != null) {
            error();
        }

        TypeClass father = null;
        if (superClassName != null) {
            Type t = sym.find(superClassName);
            if (!(t instanceof TypeClass)) {
                error();
            }
            father = (TypeClass) t;
        }

        TypeClass currClass = new TypeClass(father, name, null);
        sym.enter(name, currClass, null, 0);
        allClasses.add(currClass);
        classAstByName.put(name, this);

        sym.beginClassScope();

        AstDecClass.currentClass = currClass;

        if (father != null) {
            for (TypeClass c = father; c != null; c = c.father) {
                TypeClassVarDecList members = c.dataMembers;
                while (members != null) {
                    TypeClassVarDec dec = members.head;

                    if (!(dec.t instanceof TypeFunction)) {
                        sym.enter(dec.name, dec.t, null, 0);
                    }

                    members = members.tail;
                }
            }
        }

        sym.setVarKind(SymbolTable.VarKind.FIELD);
        TypeClassVarDecList collectedMembers = semantClassFields(classFields);
        sym.clearVarKind();

        AstDecClass.currentClass = null;
        sym.endClassScope();

        currClass.dataMembers = collectedMembers;

        // TODO: Check if we need it
        int fieldIndex = 0;
        
        TypeClass ancestor = father;
        while (ancestor != null) {
            TypeClassVarDecList m = ancestor.dataMembers;
            while (m != null) {
                if (!(m.head.t instanceof TypeFunction)) { 
                    fieldIndex++;
                }
                m = m.tail;
            }
            ancestor = ancestor.father;
        }


        TypeClassVarDecList currMember = collectedMembers;
        while (currMember != null) {
            if (!(currMember.head.t instanceof TypeFunction)) {
                currMember.head.offset = fieldIndex;
                fieldIndex++;
            }
            currMember = currMember.tail;
        }

        return null;
    }


    private TypeClassVarDecList semantClassFields(AstCFieldList fields) {
        if (fields == null) {
            return null;
        }

        AstDec d = fields.head;
        TypeClassVarDecList tailList;

        if (d instanceof AstDecVar) {
            AstDecVar v = (AstDecVar) d;

            v.semantMe();

            Type fieldType = symboltable.SymbolTable
                    .getInstance()
                    .findInCurrentScope(v.name);

            if (fieldType == null) {
                error();
            }

            tailList = semantClassFields(fields.tail);

            return new TypeClassVarDecList(
                    new TypeClassVarDec(fieldType, v.name),
                    tailList
            );
        }

        if (d instanceof AstDecFunc) {
            AstDecFunc f = (AstDecFunc) d;

            f.semantMe();

            Type funcType = symboltable.SymbolTable
                    .getInstance()
                    .findInCurrentScope(f.name);

            if (!(funcType instanceof TypeFunction)) {
                error();
            }

            tailList = semantClassFields(fields.tail);

            return new TypeClassVarDecList(
                    new TypeClassVarDec(funcType, f.name),
                    tailList
            );
        }

        return semantClassFields(fields.tail);
    }

    @Override
    public Temp irMe()
    {
        // A class declaration should not allocate its fields as globals.
        // Fields are per-instance and will be handled in object construction.
        // Here we only emit IR for methods.

        for (AstCFieldList it = classFields; it != null; it = it.tail)
        {
            if (it.head instanceof AstDecFunc)
            {
                AstDecFunc f = (AstDecFunc) it.head;

                // Recommended: mangle method label to avoid collisions:
                // e.g. "Point_move" instead of "move"
                String originalName = f.name;
                f.name = this.name + "_" + originalName;

                f.irMe();

                // restore AST name (so printing/semantics aren’t affected)
                f.name = originalName;
            }
            // else if (it.head instanceof AstDecVar) => do nothing
        }

        return null;
    }

}
