package types;

public class TypeUtils {

    // Check if src type can be assigned to dst type
    public static boolean isAssignable(Type dst, Type src) {
        if (dst == null || src == null) return false;

        if (dst == src) return true;

        // Same primitive-like types (int, string, nil, void...) by class
        if (dst.getClass() == src.getClass() && !(dst instanceof TypeClass) && !(dst instanceof TypeArray)) {
            return true;
        }

        // Arrays: only same named type is interchangeable,
        // BUT allow assigning/passing an anonymous "new T[...]" array into a named array typedef of same element type.
        if (dst instanceof TypeArray && src instanceof TypeArray) {
            TypeArray d = (TypeArray) dst;
            TypeArray s = (TypeArray) src;

            // same typedef object
            if (d == s) return true;

            // anonymous array (from "new") can flow into a named typedef over same element type
            if (s.name == null && d.name != null && d.elementType == s.elementType) return true;

            return false;
        }


        if (src instanceof TypeNil && 
           (dst instanceof TypeClass || dst instanceof TypeArray))
            return true;

        if (dst instanceof TypeClass && src instanceof TypeClass) {
            TypeClass need = (TypeClass)dst;
            TypeClass cur  = (TypeClass)src;

            while (cur != null) {
                if (cur == need) return true;
                cur = cur.father;
            }
        }

        return false;
    }
}
