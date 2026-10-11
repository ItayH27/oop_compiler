package types;

import java.util.ArrayList;
import java.util.List;

public class TypeClass extends Type
{
    public class MethodEntry {
        public String methodName;   // "m2"
        public String implLabel;    // "B_m2"

        public MethodEntry(String methodName, String implLabel) {
            this.methodName = methodName;
            this.implLabel = implLabel;
        }
    }

    public List<MethodEntry> vtable = null;
    public String name;
    public TypeClass father;
    public TypeClassVarDecList dataMembers;
	
    public TypeClass(TypeClass father, String name, TypeClassVarDecList dataMembers)
    {
        this.name = name;
        this.father = father;
        this.dataMembers = dataMembers;
    }

    @Override
    public boolean isClass() { return true; }

    public int getTotalFieldCount()
    {
        int count = 0;

        // inherited fields
        if (father != null)
            count += father.getTotalFieldCount();

        // fields declared in this class
        for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail) {
            if (!(it.head.t instanceof TypeFunction)) {
                count++;
            }
        }

        return count;
    }

    public List<MethodEntry> getVtable() {
        if (vtable == null) {
            vtable = buildVtable();
        }
        return vtable;
    }

    private List<MethodEntry> buildVtable() {
        List<MethodEntry> table = new ArrayList<>();

        if (father != null) {
            for (MethodEntry e : father.getVtable()) {
                table.add(new MethodEntry(e.methodName, e.implLabel));
            }
        }

        for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail) {
            TypeClassVarDec dec = it.head;

            if (dec.t instanceof TypeFunction) {
                String methodName = dec.name;
                String implLabel = "func_" + name + "_" + methodName;

                boolean replaced = false;
                for (int i = 0; i < table.size(); i++) {
                    if (table.get(i).methodName.equals(methodName)) {
                        table.set(i, new MethodEntry(methodName, implLabel));
                        replaced = true;
                        break;
                    }
                }

                if (!replaced) {
                    table.add(new MethodEntry(methodName, implLabel));
                }
            }
        }

        return table;
    }

    public int getMethodOffset(String methodName) {
        List<MethodEntry> table = getVtable();
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).methodName.equals(methodName)) {
                return i * 4;
            }
        }
        return -1;
    }

    public int getFieldOffset(String name) {
        int inherited = (father == null) ? 0 : father.getTotalFieldCount();

        int i = 0;
        for (TypeClassVarDecList it = dataMembers; it != null; it = it.tail) {
            if (!(it.head.t instanceof TypeFunction)) {
                if (it.head.name.equals(name)) {
                    return inherited + i;
                }
                i++;
            }
        }

        if (father != null) {
            return father.getFieldOffset(name);
        }

        return -1;
    }
}

