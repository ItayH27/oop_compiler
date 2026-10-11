package regalloc;

import java.util.*;

public class InterferenceGraph {
    public final Map<String, Set<String>> adj = new HashMap<>();

    public void addNode(String t) {
        adj.computeIfAbsent(t, k -> new HashSet<>());
    }

    public void addEdge(String a, String b) {
        if (a == null || b == null) return;
        if (a.equals(b)) return;
        addNode(a);
        addNode(b);
        adj.get(a).add(b);
        adj.get(b).add(a);
    }

    public Set<String> nodes() {
        return adj.keySet();
    }

    public Set<String> neighbors(String v) {
        return adj.getOrDefault(v, Collections.emptySet());
    }

}