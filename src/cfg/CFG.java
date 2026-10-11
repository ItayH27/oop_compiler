package cfg;

import ir.*;
import java.io.PrintWriter;
import java.util.*;
import temp.*;

/**
 * Control Flow Graph for a single IR stream.
 */
public class CFG {

    /**********************/
    /* Singleton plumbing */
    /**********************/
    private static CFG instance = null;

    private CFG() { }

    public static CFG getInstance() {
        if (instance == null) {
            instance = new CFG();
        }
        return instance;
    }

    /***********************/
    /* CFG stored contents */
    /***********************/

    /** Nodes in program order. */
    private final List<CFGNode> nodes = new ArrayList<>();

    /** Entry node (often nodes.get(0) or label "main"). */
    private CFGNode entry = null;

    /** Nodes with no successors (optional, convenient). */
    private final Set<CFGNode> exits = new HashSet<>();

    /** Map labelName -> index of the label node in 'nodes'. */
    private final Map<String, Integer> labelToIndex = new HashMap<>();

    /** Whether CFG has been built for the current IR. */
    private boolean built = false;

    public Set<String> allVariables = new HashSet<>();

    /********************/
    /* Getters / State  */
    /********************/
    public List<CFGNode> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public CFGNode getEntry() {
        return entry;
    }

    public Set<CFGNode> getExits() {
        return Collections.unmodifiableSet(exits);
    }

    public Map<String, Integer> getLabelToIndex() {
        return Collections.unmodifiableMap(labelToIndex);
    }

    public boolean isBuilt() {
        return built;
    }

    /****************************/
    /* Mutation / lifecycle API */
    /****************************/
    /**
     * Clear all CFG state (useful between tests in the same JVM).
     */
    public void reset() {
        nodes.clear();
        exits.clear();
        labelToIndex.clear();
        entry = null;
        built = false;
    }

    /*
     * Build the CFG from the CURRENT IR.
     * This is where Ir.toList() is used.
     */
    public void buildFromIr() {
        reset();

        List<IrCommand> irList = Ir.getInstance().toList();

        // -------------------------
        // Section 3: first pass
        // -------------------------
        for (int i = 0; i < irList.size(); i++) {
            IrCommand cmd = irList.get(i);

            String labelName = null;
            String targetLabel = null;

            if (cmd instanceof IrCommandLabel) {
                labelName = ((IrCommandLabel) cmd).labelName; // adjust field name if needed
                labelToIndex.put(labelName, i);
            }

            CFGNode n = new CFGNode(i, cmd, labelName, targetLabel);
            fillUsesDefs(n);
            allVariables.addAll(n.uses);
            allVariables.addAll(n.defs);
            nodes.add(n);
        }

        if (!nodes.isEmpty()) entry = nodes.get(0);

        // -------------------------
        // Section 4: second pass (EDGES)
        // -------------------------
        for (int i = 0; i < nodes.size(); i++) {
            CFGNode cur = nodes.get(i);
            IrCommand cmd = cur.ir;

            // Helper: fall-through successor (i+1) if exists
            CFGNode next = (i + 1 < nodes.size()) ? nodes.get(i + 1) : null;

            // A) Unconditional jump: edge to instruction after label, no fall-through
            if (cmd instanceof IrCommandJumpLabel) {
                String target = ((IrCommandJumpLabel) cmd).labelName; // adjust field name
                CFGNode targetNode = nodeAfterLabel(target);
                if (targetNode != null) {
                    cur.addSucc(targetNode);
                }
                // no fall-through
            }

            // B) Conditional jump: edge to target AND to next
            else if (cmd instanceof IrCommandJumpIfEqToZero) {
                String target = ((IrCommandJumpIfEqToZero) cmd).labelName; // adjust field name
                CFGNode targetNode = nodeAfterLabel(target);
                if (targetNode != null) {
                    cur.addSucc(targetNode);
                }
                if (next != null) {
                    cur.addSucc(next);
                }
            }

            // C) Otherwise: fall-through
            else {
                if (next != null) {
                    cur.addSucc(next);
                }
            }
        }

        // Fill exits = nodes with no successors
        for (CFGNode n : nodes) {
            if (n.succ().isEmpty()) {
                exits.add(n);
            }
        }

        built = true;
    }

    /**
     * Returns the CFG node for "the instruction following the target label",
     * i.e. labelIndex(label) + 1, if it exists.
     */
    private CFGNode nodeAfterLabel(String label) {
        Integer labelIdx = labelToIndex.get(label);
        if (labelIdx == null) return null;

        int targetIdx = labelIdx + 1;
        if (targetIdx < 0 || targetIdx >= nodes.size()) return null;

        return nodes.get(targetIdx);
    }

    /**
     * Mark this CFG as built. Call this at the end of your CFG building procedure.
     */
    public void markBuilt() {
        this.built = true;
    }

    /**
     * Package-private helpers for a builder class in the same package.
     * (Keeps CFG as a data container; builder does the algorithm.)
     */
    void addNode(CFGNode n) {
        nodes.add(n);
    }

    void setEntry(CFGNode entry) {
        this.entry = entry;
    }

    void addExit(CFGNode n) {
        exits.add(n);
    }

    void putLabel(String label, int index) {
        labelToIndex.put(label, index);
    }

    public void dump(PrintWriter out) {
        out.println("=== CFG DUMP ===");
        out.println("nodes=" + nodes.size());

        if (entry != null) {
            out.println("entry=" + entry.id);
        }

        for (CFGNode n : nodes) {
            out.print("[" + n.id + "] ");

            // IR instruction (relies on IR command toString(), like your IR dump)
            out.print(n.ir);

            // Successors
            out.print("  succ={");
            for (int i = 0; i < n.succ().size(); i++) {
                if (i > 0) out.print(",");
                out.print(n.succ().get(i).id);
            }
            
            out.print("}");

            out.print(" pred={");
            for (int i = 0; i < n.pred().size(); i++) {
                if (i > 0) out.print(",");
                out.print(n.pred().get(i).id);
            }
            out.println("}");

            // Uses and defs prints
            out.print(" uses=" + n.uses);
            out.print(" defs=" + n.defs);
        }

        out.println("exits=" + exitsToString());
        out.println("=== END CFG DUMP ===");
        out.flush();
    }

    private String exitsToString() {
        List<Integer> ids = new ArrayList<>();
        for (CFGNode e : exits) ids.add(e.id);
        Collections.sort(ids);
        return ids.toString();
    }

    private void fillUsesDefs(CFGNode n) {
        IrCommand cmd = n.ir;

        switch (cmd) {
            /* ==== Memory variables ==== */
            case IrCommandAllocate allocCmd:
                n.defs.add(allocCmd.varName);
                break;
            case IrCommandLoad loadCmd:
                n.defs.add(loadCmd.dst.toString());
                n.uses.add(loadCmd.varName);
                break;
            case IrCommandStore storeCmd:
                n.uses.add(storeCmd.src.toString());
                n.defs.add(storeCmd.varName);
                break;
            /* ==== Arithmethic / comparisons ==== */
            case IrCommandBinop binopCmd:
                n.defs.add(binopCmd.dst.toString());
                n.uses.add(binopCmd.t1.toString());
                n.uses.add(binopCmd.t2.toString());
                break;
            case IrCommandBinopConcatStrings concatCmd:
                n.defs.add(concatCmd.dst.toString());
                n.uses.add(concatCmd.t1.toString());
                n.uses.add(concatCmd.t2.toString());
                break;
            case IrCommandBinopEqStrings eqStrCmd:
                n.defs.add(eqStrCmd.dst.toString());
                n.uses.add(eqStrCmd.t1.toString());
                n.uses.add(eqStrCmd.t2.toString());
                break;
            
            /* ==== Constant and address ==== */
            case IRcommandConstInt constIntCmd:
                n.defs.add(constIntCmd.t.toString());
                break;
            case IrCommandLoadAddress loadAddrCmd:
                n.defs.add(loadAddrCmd.dst.toString());
                break;
            /* ==== Control flow ==== */
            case IrCommandJumpIfEqToZero jumpIfCmd:
                n.uses.add(jumpIfCmd.t.toString());
                break;
            /* ==== Built-in functions ==== */
            case IrCommandPrintInt printIntCmd:
                n.uses.add(printIntCmd.t.toString());
                break;
            case IrCommandPrintString printStrCmd:
                n.uses.add(printStrCmd.str.toString());
                break;
            /* ==== Function calls and returns ==== */
            case IrCommandCall callCmd:
                // args are USED
                if (callCmd.args != null) {
                    for (var t : callCmd.args) {
                        if (t != null) n.uses.add(t.toString());
                    }
                }
                // dst is DEFINED (if function returns a value)
                if (callCmd.dst != null) {
                    n.defs.add(callCmd.dst.toString());
                }
                break;
            case IrCommandReturn retCmd:
                if (retCmd.retVal != null) {
                    n.uses.add(retCmd.retVal.toString());
                }
                break;
            /* ==== Heap / Arrays / Fields ==== */
            case IrCommandMalloc mallocCmd:
                n.defs.add(mallocCmd.dst.toString());
                break;
            case IrCommandNewArray newArrayCmd:
                n.defs.add(newArrayCmd.dst.toString());
                n.uses.add(newArrayCmd.len.toString());
                break;
            case IrCommandArrayAccess arrayAccessCmd:
                n.defs.add(arrayAccessCmd.dst.toString());
                n.uses.add(arrayAccessCmd.array.toString());
                n.uses.add(arrayAccessCmd.index.toString());
                break;
            case IrCommandArraySet arraySetCmd:
                n.uses.add(arraySetCmd.array.toString());
                n.uses.add(arraySetCmd.index.toString());
                n.uses.add(arraySetCmd.value.toString());
                break;
            case IrCommandFieldAccess fieldAccessCmd:
                n.defs.add(fieldAccessCmd.dst.toString());
                n.uses.add(fieldAccessCmd.obj.toString());
                break;
            case IrCommandFieldSet fieldSetCmd:
                n.uses.add(fieldSetCmd.obj.toString());
                n.uses.add(fieldSetCmd.value.toString());
                break;
            case IrCommandLoadAccess loadAccessCmd:
                n.defs.add(loadAccessCmd.dst.toString());
                n.uses.add(loadAccessCmd.ref.toString());
                break;
            case IrCommandVirtualCall vcallCmd:
                if (vcallCmd.objTemp != null) {
                    n.uses.add(vcallCmd.objTemp.toString());
                }
                if (vcallCmd.args != null) {
                    for (Temp t : vcallCmd.args) {
                        if (t != null) n.uses.add(t.toString());
                    }
                }
                if (vcallCmd.dst != null) {
                    n.defs.add(vcallCmd.dst.toString());
                }
                break;
            default:
                // Do nothing
                break;
        }
    }


}