package cfg;

import ir.IrCommand;
import java.util.*;

/**
 * One CFG node per IR instruction.
 */
public class CFGNode {

    /** Stable id (usually the instruction index in the flattened IR list). */
    public final int id;

    /** The underlying IR instruction represented by this node. */
    public final IrCommand ir;

    /** Successors (outgoing edges). */
    private final List<CFGNode> succ = new ArrayList<>();

    /** Predecessors (incoming edges). */
    private final List<CFGNode> pred = new ArrayList<>();

    /** Optional: if this node is a label instruction, store its label name. */
    public final String labelName; // null if not a label

    /** Optional: if this node is a branch/jump, store the target label name. */
    public final String targetLabel; // null if not a branch/jump

    /** Placeholders for later dataflow work (liveness etc.). */
    public final Set<String> uses = new HashSet<>();
    public final Set<String> defs = new HashSet<>();

    public CFGNode(int id, IrCommand ir, String labelName, String targetLabel) {
        this.id = id;
        this.ir = ir;
        this.labelName = labelName;
        this.targetLabel = targetLabel;
    }

    public List<CFGNode> succ() { return Collections.unmodifiableList(succ); }
    public List<CFGNode> pred() { return Collections.unmodifiableList(pred); }

    /** Add edge this -> to (also updates to.pred). */
    public void addSucc(CFGNode to) {
        if (to == null) return;
        succ.add(to);
        to.pred.add(this);
    }

    @Override
    public String toString() {
        return "CFGNode{id=" + id + ", ir=" + ir + "}";
    }
}
