package regalloc;

import cfg.LivenessAnalyzer;

import java.util.*;

public class RegisterAllocator {
    public static final int K = 10;
    private static final String[] REG_POOL = {
            "$t0", "$t1", "$t2", "$t3", "$t4",
            "$t5", "$t6", "$t7", "$t8", "$t9"
    };

    public static class AllocationFailed extends RuntimeException {
        public AllocationFailed(String msg) { super(msg); }
    }

    /**
     * Returns: funcLabel -> (tempName -> "$tX")
     */
    public static Map<String, Map<String, String>> allocateAll(LivenessAnalyzer.Result live) {
        Map<String, Map<String, String>> funcToTempReg = new LinkedHashMap<>();

        // Build interference graph and color for each function
        for (String func : live.inByFunc.keySet()) {
            InterferenceGraph g = InterferenceBuilder.buildForFunction(func, live);
            
            // Color the graph without spilling
            Map<String, Integer> colors = colorNoSpill(g, K);

            // Map temp names to actual registers
            Map<String, String> tempToReg = new HashMap<>();
            for (Map.Entry<String, Integer> e : colors.entrySet()) {
                tempToReg.put(e.getKey(), REG_POOL[e.getValue()]);
            }

            funcToTempReg.put(func, tempToReg);
        }

        return funcToTempReg;
    }

    // ----------------------------
    // Coloring: simplify/select, no spilling
    // ----------------------------
    private static Map<String, Integer> colorNoSpill(InterferenceGraph g, int K) {
        // Mutable copy for simplify
        Map<String, Set<String>> adj = new HashMap<>();
        for (String v : g.nodes()) {
            adj.put(v, new HashSet<>(g.neighbors(v)));
        }

        Deque<String> stack = new ArrayDeque<>();
        Set<String> removed = new HashSet<>();

        // Simplify
        while (removed.size() < adj.size()) {
            String v = pickLowestDegree(adj, removed, K);
            if (v == null) {
                throw new AllocationFailed("Needs spill (no node with degree < " + K + ")");
            }

            removed.add(v);
            stack.push(v);

            // Remove v
            for (String nb : new ArrayList<>(adj.getOrDefault(v, Collections.emptySet()))) {
                if (!removed.contains(nb)) {
                    adj.get(nb).remove(v);
                }
            }
        }

        // Select
        Map<String, Integer> color = new HashMap<>();
        while (!stack.isEmpty()) {
            String v = stack.pop();
            boolean[] used = new boolean[K];

            // Mark colors used by neighbors
            for (String nb : g.neighbors(v)) {
                Integer c = color.get(nb);
                if (c != null) used[c] = true;
            }

            // Pick the first available color
            int chosen = -1;
            for (int c = 0; c < K; c++) {
                if (!used[c]) { chosen = c; break; }
            }
            
            // If no color is available, this means the graph is not K-colorable
            if (chosen == -1) {
                throw new AllocationFailed("Select failed for " + v);
            }
            color.put(v, chosen);
        }

        return color;
    }

    private static String pickLowestDegree(Map<String, Set<String>> adj, Set<String> removed, int K) {
        String candidate = null;
        int minDeg = Integer.MAX_VALUE;

        for (String v : adj.keySet()) {
            if (removed.contains(v)) continue;

            int deg = 0;
            for (String nb : adj.get(v)) {
                if (!removed.contains(nb)) deg++;
            }

            if (deg < K && deg < minDeg) {
                candidate = v;
                minDeg = deg;
            }
        }
        return candidate;
    }
}