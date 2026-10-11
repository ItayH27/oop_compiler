package cfg;

import java.util.*;

/**
 * Backward Liveness Analysis (per function).
 *
 * Domain: sets of temporaries (t0, t1, ...)
 * Meet: union
 * Direction: backward
 * Init: empty sets
 *
 * OUT[n] = union_{s in succ(n)} IN[s]
 * IN[n]  = GEN[n] U (OUT[n] \ KILL[n])
 *
 * GEN = uses(n), KILL = defs(n)
 */
public class LivenessAnalyzer {

    public static class Result {
        // functionName -> (node -> IN set)
        public final Map<String, Map<CFGNode, Set<String>>> inByFunc = new LinkedHashMap<>();
        // functionName -> (node -> OUT set)
        public final Map<String, Map<CFGNode, Set<String>>> outByFunc = new LinkedHashMap<>();
        // node -> functionName (handy for filtering succ/pred)
        public final Map<CFGNode, String> nodeToFunc = new HashMap<>();
    }

    /**
     * Main entry point.
     * Runs liveness independently for each function subgraph inside the CFG stream.
     */
    public static Result analyze(CFG cfg) {

        Result res = new Result();

        // 1) Partition nodes by function (without modifying CFG/CFGNode).
        Map<String, List<CFGNode>> nodesByFunc = partitionByFunction(cfg, res.nodeToFunc);

        // 2) Run a separate backward liveness fixpoint per function.
        for (Map.Entry<String, List<CFGNode>> entry : nodesByFunc.entrySet()) {
            String func = entry.getKey();
            List<CFGNode> funcNodes = entry.getValue();

            Map<CFGNode, Set<String>> in = new HashMap<>();
            Map<CFGNode, Set<String>> out = new HashMap<>();

            // init to empty sets
            for (CFGNode n : funcNodes) {
                in.put(n, new HashSet<>());
                out.put(n, new HashSet<>());
            }

            // Worklist: for backward analysis, it’s common to start with all nodes.
            ArrayDeque<CFGNode> worklist = new ArrayDeque<>(funcNodes);

            while (!worklist.isEmpty()) {
                CFGNode n = worklist.poll();

                // OUT[n] = union of IN[succ] (only succ in same function)
                Set<String> newOut = new HashSet<>();
                for (CFGNode s : n.succ()) {
                    if (func.equals(res.nodeToFunc.get(s))) {
                        newOut.addAll(in.get(s));
                    }
                }

                // IN[n] = GEN[n] U (OUT[n] \ KILL[n])
                Set<String> gen = genTemps(n);
                Set<String> kill = killTemps(n);

                Set<String> newIn = new HashSet<>(newOut);
                newIn.removeAll(kill);
                newIn.addAll(gen);

                boolean changed = !newOut.equals(out.get(n)) || !newIn.equals(in.get(n));
                if (changed) {
                    out.put(n, newOut);
                    in.put(n, newIn);

                    // If IN/OUT changed, predecessors may be affected (backward flow),
                    // so add preds (only in same function).
                    for (CFGNode p : n.pred()) {
                        if (func.equals(res.nodeToFunc.get(p))) {
                            worklist.add(p);
                        }
                    }
                }
            }

            res.inByFunc.put(func, in);
            res.outByFunc.put(func, out);
        }

        return res;
    }

    /**
     * GEN set: temps used in node.
     * We strongly recommend *temps only* here for register allocation.
     */
    private static Set<String> genTemps(CFGNode n) {
        Set<String> gen = new HashSet<>();
        for (String u : n.uses) {
            if (isTemp(u)) gen.add(u);
        }
        return gen;
    }

    /**
     * KILL set: temps defined in node.
     */
    private static Set<String> killTemps(CFGNode n) {
        Set<String> kill = new HashSet<>();
        for (String d : n.defs) {
            if (isTemp(d)) kill.add(d);
        }
        return kill;
    }

    private static boolean isTemp(String s) {
        return s != null && s.matches("^t\\d+$");
    }

    /**
     * Partition nodes into functions.
     *
     * Since your CFG is built from a flat IR list, we infer function boundaries from label nodes.
     * Adjust isFunctionLabel(...) to match your IR naming conventions.
     *
     * Fallback: if we never detect a function label, everything goes to "global".
     */
    private static Map<String, List<CFGNode>> partitionByFunction(CFG cfg, Map<CFGNode, String> nodeToFunc) {
        Map<String, List<CFGNode>> nodesByFunc = new LinkedHashMap<>();

        String currentFunc = "global";

        for (CFGNode n : cfg.getNodes()) {
            // If this node is a label and looks like a function entry, start a new function "region".
            if (n.labelName != null && isFunctionLabel(n.labelName)) {
                currentFunc = n.labelName;
            }

            nodesByFunc.computeIfAbsent(currentFunc, k -> new ArrayList<>()).add(n);
            nodeToFunc.put(n, currentFunc);
        }

        return nodesByFunc;
    }

    /**
     * Heuristic: decide whether a label marks a function entry.
     * You MUST adapt this to your project’s label naming.
     *
     * Common patterns:
     * - "main"
     * - labels that start with "func_" / "Function_" / etc.
     * - labels you emit for method prologues
     */
    private static boolean isFunctionLabel(String label) {
        if (label == null) return false;
        if (label.startsWith("func_end_")) return false;
        if (label.equals("func_main")) return false;
        if (label.startsWith("Label_")) return false;
        // if (label.startsWith("func_")) return true;
        // if (label.startsWith("Function_")) return true;
        // add more rules if needed
        return true;
    }
}
