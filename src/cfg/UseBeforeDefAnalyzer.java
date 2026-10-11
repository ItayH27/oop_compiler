package cfg;

import java.util.*;
import java.io.PrintWriter;

import ir.*;
import cfg.CFG;
import cfg.CFGNode;

public class UseBeforeDefAnalyzer {

    static class Def {
        String varName;
        Integer label;

        Def(String varName, Integer label) {
            this.varName = varName;
            this.label = label; // can be null
        }

        @Override
        public String toString() {
            if (label == null) {
                return "(" + varName + ", ?)";
            }
            return "(" + varName + ", " + label + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Def)) return false;
            Def other = (Def) o;
            return Objects.equals(varName, other.varName) &&
                Objects.equals(label, other.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(varName, label);
        }
    }

    

    static SortedSet<String> useBeforeDef = new TreeSet<>();

    public static SortedSet<String> analyze(CFG cfg) {
        useBeforeDef.clear();

        // Initialize
        CFGNode entry = cfg.getEntry();
        Map<CFGNode, Set<Def>> in = new HashMap<>();
        Map<CFGNode, Set<Def>> out = new HashMap<>();

        Set<Def> entryIn = new HashSet<>();
        for (String v : cfg.allVariables) {
            entryIn.add(new Def(v, null));  // (v, ?)
        }
        in.put(entry, entryIn);
        out.put(entry, new HashSet<>());

        for (int i = 1; i < cfg.getNodes().size(); i++) {
            CFGNode n = cfg.getNodes().get(i);
            in.put(n, new HashSet<>());
            out.put(n, new HashSet<>());
        }

        // Worklist algorithm
        Queue<CFGNode> worklist = new LinkedList<>();
        worklist.add(entry);

        while (!worklist.isEmpty()) {
            CFGNode n = worklist.poll();

            Set<Def> newIn;
            if (n == entry) {
                // IN[entry] = {(v, ?) | v in Vars}
                newIn = in.get(entry);
            } else {
                // IN[n] = union of OUT[pred]
                newIn = new HashSet<>();
                for (CFGNode p : n.pred()) {
                    newIn.addAll(out.get(p));
                }
            }

            boolean inChanged = !newIn.equals(in.get(n));
            if (inChanged) {
                in.put(n, newIn);
            }

            Set<Def> newOut = transfer(n, newIn);
            if (!newOut.equals(out.get(n))) {
                out.put(n, newOut);
                worklist.addAll(n.succ());
            }

        }

        return useBeforeDef;

    }

    private static Set<Def> transfer(CFGNode n, Set<Def> in) {
        Set<Def> out = new HashSet<>(in);

        // KILL
        for (String d : n.defs) {
            out.removeIf(def -> def.varName.equals(d));
        }

        // GEN
        IrCommand cmd = n.ir;

        switch (cmd) {
            case IrCommandLoad loadCmd:
                if (isInitialized(loadCmd.varName, in)){
                    out.add(new Def(loadCmd.dst.toString(), n.id));
                }
                else {
                    out.add(new Def(loadCmd.dst.toString(), null));
                }
                break;
            case IrCommandStore storeCmd:
                if (isInitialized(storeCmd.src.toString(), in)){
                    out.add(new Def(storeCmd.varName, n.id));
                }
                else {
                    out.add(new Def(storeCmd.varName, null));
                }
                break;
            case IrCommandBinop binopCmd:
                if (isInitialized(binopCmd.t1.toString(), in) &&
                    isInitialized(binopCmd.t2.toString(), in)) {
                    out.add(new Def(binopCmd.dst.toString(), n.id));
                }
                else {
                    out.add(new Def(binopCmd.dst.toString(), null));
                }
                break;
            case IRcommandConstInt constIntCmd:
                out.add(new Def(constIntCmd.t.toString(), n.id));
                break;
            default:
                // Do nothing
                break;
        }

        for (String u : n.uses) {
            boolean usedBeforeDefined = false;
            for (Def d : in) {
                if (d.varName.equals(u) && d.label == null) {
                    usedBeforeDefined = true;
                    break;
                }
            }
            if (usedBeforeDefined && notTemp(u)) {
                useBeforeDef.add(stripSuffix(u));
            }
        }

        return out;

    }

    private static boolean isInitialized(String varName, Set<Def> in) {
        for (Def d : in) {
            if (d.varName.equals(varName) && d.label == null) {
                return false;
            }
        }
        return true;
    }

    private static boolean notTemp(String s) {
        if (s == null) return false;
        return !s.matches("^t\\d+$");   // temps look like t0, t1, ...
    }

    private static String stripSuffix(String unique) {
        int idx = unique.lastIndexOf('_');
        if (idx < 0) return unique;
        String tail = unique.substring(idx + 1);
        if (tail.matches("\\d+")) return unique.substring(0, idx);
        return unique;
    } 
}