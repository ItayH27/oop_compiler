package regalloc;

import cfg.CFGNode;
import cfg.LivenessAnalyzer;
import java.util.*;

public class InterferenceBuilder {

    public static InterferenceGraph buildForFunction(String funcName, LivenessAnalyzer.Result live) {
        Map<CFGNode, Set<String>> in = live.inByFunc.get(funcName);
        if (in == null) in = Collections.emptyMap();

        InterferenceGraph g = new InterferenceGraph();

        // 1) Add ALL temps that appear in the function as nodes
        for (CFGNode node : in.keySet()) {
            for (String s : node.uses) {
                if (isTemp(s)) g.addNode(s);
            }
            for (String s : node.defs) {
                if (isTemp(s)) g.addNode(s);
            }
        }

        // 2) Add interference edges from liveness IN sets
        for (Set<String> inSet : in.values()) {
            if (inSet == null || inSet.isEmpty()) continue;

            ArrayList<String> temps = new ArrayList<>();
            for (String s : inSet) {
                if (isTemp(s)) {
                    g.addNode(s);
                    temps.add(s);
                }
            }

            int m = temps.size();
            for (int i = 0; i < m; i++) {
                for (int j = i + 1; j < m; j++) {
                    g.addEdge(temps.get(i), temps.get(j));
                }
            }
        }

        return g;
    }

    private static boolean isTemp(String s) {
        return s != null && s.matches("^t\\d+$"); // adapt if your temp names differ
    }
}