

package mips;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import temp.Temp;
import types.TypeClass;
import types.TypeClass.MethodEntry;

public class MipsGenerator {
    private static final int WORD_SIZE = 4;
    private static final int SAVED_T_COUNT = 10; // $t0-$t9

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static MipsGenerator instance = null;

    /*****************************/
    /* PREVENT INSTANTIATION ... */
    /*****************************/
    protected MipsGenerator() {}

    /******************************/
    /* GET SINGLETON INSTANCE ... */
    /******************************/
    public static MipsGenerator getInstance() {
        if (instance == null) {
            instance = new MipsGenerator();
            try {
                String dirname = "./output/";
                String filename = "MIPS.txt";
                instance.fileWriter = new PrintWriter(dirname + filename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    /***********************/
    /* The actual writer   */
    /***********************/
    private PrintWriter fileWriter;

    /***********************/
    /* Output buffers      */
    /***********************/
    private final StringBuilder dataBuffer = new StringBuilder();
    private final StringBuilder runtimeBuffer = new StringBuilder();
    private final StringBuilder textBuffer = new StringBuilder();

    /***********************/
    /* Runtime requirements */
    /***********************/
    private boolean needAccessViolation = false;
    private boolean needDivByZero = false;
    private boolean needInvalidPtr = false;
    private boolean needConcatStrings = false;
    private boolean needStrEq = false;
    private boolean needSaturation = false;

    /***********************/
    /* One-time emissions  */
    /***********************/
    private boolean runtimeDataEmitted = false;
    private boolean runtimeHelpersEmitted = false;

    /***********************/
    /* General state       */
    /***********************/
    private int labelCounter = 0;
    private int currentLocalsBytes = 0;

    /***********************/
    /* Per-function maps   */
    /***********************/
    private Map<String, Integer> funcLocalsBytes = null;
    private Map<String, Map<String, String>> funcRegMaps = null;
    private Map<String, String> currentRegMap = null;

    /***********************/
    /* Setters from Main   */
    /***********************/
    public void setFileWriter(PrintWriter pw) {
        this.fileWriter = pw;

        needAccessViolation = false;
        needDivByZero = false;
        needInvalidPtr = false;
        needConcatStrings = false;
        needStrEq = false;
        needSaturation = false;

        runtimeDataEmitted = false;
        runtimeHelpersEmitted = false;

        labelCounter = 0;
        currentLocalsBytes = 0;
        currentRegMap = (funcRegMaps != null ? funcRegMaps.get("global") : null);

        dataBuffer.setLength(0);
        runtimeBuffer.setLength(0);
        textBuffer.setLength(0);
    }

    public void setFuncLocalsBytes(Map<String, Integer> localsBytes) {
        this.funcLocalsBytes = localsBytes;
    }

    public void setFuncRegMaps(Map<String, Map<String, String>> maps) {
        this.funcRegMaps = maps;
        this.currentRegMap = (maps != null ? maps.get("global") : null);
    }

    public void setCurrentLocalsBytes(int bytes) {
        if (bytes % 4 != 0) {
            bytes += (4 - (bytes % 4));
        }
        this.currentLocalsBytes = bytes;
    }

    public int getCurrentLocalsBytes() {
        return currentLocalsBytes;
    }

    /***********************/
    /* Buffer writers      */
    /***********************/
    private void emitData(String s) {
        dataBuffer.append(s);
    }

    private void emitRuntime(String s) {
        runtimeBuffer.append(s);
    }

    private void emitText(String s) {
        textBuffer.append(s);
    }

    /***********************/
    /* Runtime requirement */
    /***********************/
    public void requireAccessViolation() { needAccessViolation = true; }
    public void requireDivByZero() { needDivByZero = true; }
    public void requireInvalidPtr() { needInvalidPtr = true; }
    public void requireConcatStrings() { needConcatStrings = true; }
    public void requireStrEq() { needStrEq = true; }
    public void requireSaturation() { needSaturation = true; }

    /***********************/
    /* Register resolver   */
    /***********************/
    private String r(Temp t) {
        if (t == null) return "$zero";
        if (currentRegMap == null) {
            throw new RuntimeException("currentRegMap is null for temp " + t);
        }
        String reg = currentRegMap.get(t.toString());
        if (reg == null) {
            throw new RuntimeException("Unallocated temp " + t);
        }
        return reg;
    }

    /***********************/
    /* Helpers             */
    /***********************/
    private Integer parseIndex(String s, String prefix) {
        if (!s.startsWith(prefix)) return null;
        return Integer.parseInt(s.substring(prefix.length()));
    }

    private boolean isParam(String v) { return v.startsWith("param_"); }
    private boolean isLocal(String v) { return v.startsWith("local_"); }

    private int paramOffset(int idx1Based) {
        return 4 * (idx1Based + 1); // param_1 -> 8($fp)
    }

    private int localOffset(int idx1Based) {
        return -4 * (SAVED_T_COUNT + idx1Based); // local_1 -> -44($fp)
    }

    public String getFreshLabel(String prefix) {
        return prefix + "_" + labelCounter++;
    }

    public String freshLabel(String base) {
        return String.format("Label_%d_%s", labelCounter++, base);
    }

    /***********************/
    /* Data allocation     */
    /***********************/
    public void allocate(String varName) {
        if (isLocal(varName) || isParam(varName)) return;
        emitData(String.format("global_%s: .word 0\n", varName));
    }

    public void allocateGlobalInt(String varName, int value) {
        emitData(String.format("global_%s: .word %d\n", varName, value));
    }

    public void allocateGlobalLabelAddress(String varName, String label) {
        emitData(String.format("global_%s: .word %s\n", varName, label));
    }

    public void allocateString(String label, String value) {
        String cleanValue = value;
        if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
            cleanValue = cleanValue.substring(1, cleanValue.length() - 1);
        }
        cleanValue = cleanValue
                .replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
        emitData(String.format("%s: .asciiz \"%s\"\n", label, cleanValue));
    }

    public void emitVtable(String vtableLabel, String[] methodLabels) {
        emitData(String.format("%s:\n", vtableLabel));
        for (String methodLabel : methodLabels) {
            emitData(String.format("\t.word %s\n", methodLabel));
        }
    }

    public void emitVtable(TypeClass cls) {
        emitData("vt_" + cls.name + ":\n");
        for (MethodEntry e : cls.getVtable()) {
            emitData("\t.word " + e.implLabel + "\n");
        }
    }

    public void emitAllVtables(List<TypeClass> classes) {
        for (TypeClass cls : classes) {
            emitVtable(cls);
        }
    }

    /***********************/
    /* Stack helpers       */
    /***********************/
    public void push(Temp t) {
        emitText("\tsubu $sp,$sp,4\n");
        emitText(String.format("\tsw %s,0($sp)\n", r(t)));
    }

    public void popBytes(int bytes) {
        if (bytes != 0) emitText(String.format("\taddi $sp,$sp,%d\n", bytes));
    }

    public void moveV0To(Temp dst) {
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    public void moveFromV0(Temp dst) {
        moveV0To(dst);
    }

    public void moveToV0(Temp src) {
        emitText(String.format("\tmove $v0,%s\n", r(src)));
    }

    public void moveToA0(Temp src) {
        emitText(String.format("\tmove $a0,%s\n", r(src)));
    }

    public void moveToA1(Temp src) {
        emitText(String.format("\tmove $a1,%s\n", r(src)));
    }

    /***********************/
    /* Simple text ops     */
    /***********************/
    public void label(String inlabel) {
        if (funcRegMaps != null && funcRegMaps.containsKey(inlabel)) {
            currentRegMap = funcRegMaps.get(inlabel);
        }

        emitText(String.format("%s:\n", inlabel));

        if (inlabel.startsWith("func_end_")) {
            functionEpilogueAndReturn();
            return;
        }

        boolean isFunctionEntry =
                (funcRegMaps != null && funcRegMaps.containsKey(inlabel) && !inlabel.equals("main"));

        if (isFunctionEntry) {
            int bytes = 0;
            if (funcLocalsBytes != null && funcLocalsBytes.containsKey(inlabel)) {
                bytes = funcLocalsBytes.get(inlabel);
            }
            setCurrentLocalsBytes(bytes);
            functionPrologue();
        }
    }

    public void jump(String inlabel) {
        emitText(String.format("\tj %s\n", inlabel));
    }

    public void jal(String funcName) {
        emitText(String.format("\tjal %s\n", funcName));
    }

    public void jr(String reg) {
        emitText(String.format("\tjr %s\n", reg));
    }

    public void move(Temp dst, Temp src) {
        emitText(String.format("\tmove %s,%s\n", r(dst), r(src)));
    }

    public void li(Temp t, int value) {
        emitText(String.format("\tli %s,%d\n", r(t), value));
    }

    public void la(Temp dst, String label) {
        emitText(String.format("\tla %s,%s\n", r(dst), label));
    }

    public void addi(Temp dst, Temp src, int imm) {
        emitText(String.format("\taddi %s,%s,%d\n", r(dst), r(src), imm));
    }

    public void malloc(Temp dst, int bytes) {
        emitText(String.format("\tli $a0,%d\n", bytes));
        emitText("\tli $v0,9\n");
        emitText("\tsyscall\n");
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    public void malloc(Temp dst, Temp sizeInBytes) {
        emitText(String.format("\tmove $a0,%s\n", r(sizeInBytes)));
        emitText("\tli $v0,9\n");
        emitText("\tsyscall\n");
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    public void load(Temp dst, String varName) {
        Integer p = parseIndex(varName, "param_");
        if (p != null) {
            emitText(String.format("\tlw %s, %d($fp)\n", r(dst), paramOffset(p)));
            return;
        }

        Integer l = parseIndex(varName, "local_");
        if (l != null) {
            emitText(String.format("\tlw %s, %d($fp)\n", r(dst), localOffset(l)));
            return;
        }

        emitText(String.format("\tlw %s, global_%s\n", r(dst), varName));
    }

    public void lw(Temp dst, String varName) {
        load(dst, varName);
    }

    public void store(String varName, Temp src) {
        Integer p = parseIndex(varName, "param_");
        if (p != null) {
            emitText(String.format("\tsw %s, %d($fp)\n", r(src), paramOffset(p)));
            return;
        }

        Integer l = parseIndex(varName, "local_");
        if (l != null) {
            emitText(String.format("\tsw %s, %d($fp)\n", r(src), localOffset(l)));
            return;
        }

        emitText(String.format("\tsw %s, global_%s\n", r(src), varName));
    }

    public void sw(String varName, Temp src) {
        store(varName, src);
    }

    public void lw(Temp dst, Temp base, int offset) {
        emitText(String.format("\tlw %s,%d(%s)\n", r(dst), offset, r(base)));
    }

    public void sw(Temp src, Temp base, int offset) {
        emitText(String.format("\tsw %s,%d(%s)\n", r(src), offset, r(base)));
    }

    /***********************/
    /* Arithmetic          */
    /***********************/
    public void add(Temp dst, Temp oprnd1, Temp oprnd2) {
        requireSaturation();
        emitText(String.format("\tadd %s,%s,%s\n", r(dst), r(oprnd1), r(oprnd2)));
        emitText(String.format("\tmove $s0,%s\n", r(dst)));
        emitText("\tmove $s3, $ra\n");
        emitText("\tjal saturate_arith\n");
        emitText("\tmove $ra, $s3\n");
        emitText(String.format("\tmove %s,$s0\n", r(dst)));
    }

    public void sub(Temp dst, Temp oprnd1, Temp oprnd2) {
        requireSaturation();
        emitText(String.format("\tsubu %s,%s,%s\n", r(dst), r(oprnd1), r(oprnd2)));
        emitText(String.format("\tmove $s0,%s\n", r(dst)));
        emitText("\tmove $s3, $ra\n");
        emitText("\tjal saturate_arith\n");
        emitText("\tmove $ra, $s3\n");
        emitText(String.format("\tmove %s,$s0\n", r(dst)));
    }

    public void mul(Temp dst, Temp oprnd1, Temp oprnd2) {
        requireSaturation();
        emitText(String.format("\tmul %s,%s,%s\n", r(dst), r(oprnd1), r(oprnd2)));
        emitText(String.format("\tmove $s0,%s\n", r(dst)));
        emitText("\tmove $s3, $ra\n");
        emitText("\tjal saturate_arith\n");
        emitText("\tmove $ra, $s3\n");
        emitText(String.format("\tmove %s,$s0\n", r(dst)));
    }

    public void div(Temp dst, Temp oprnd1, Temp oprnd2) {
        requireDivByZero();
        requireSaturation();
        String floorDone = getFreshLabel("floor_done");
        emitText(String.format("\tbeq %s,$zero,div_by_zero_error\n", r(oprnd2)));
        // Compute floor division
        emitText(String.format("\tdiv %s,%s\n", r(oprnd1), r(oprnd2)));
        emitText(String.format("\txor $s2,%s,%s\n", r(oprnd1), r(oprnd2)));
        emitText(String.format("\tmflo %s\n", r(dst)));
        emitText("\tmfhi $s1\n");
        emitText(String.format("\tbeq $s1,$zero,%s\n", floorDone));
        emitText(String.format("\tbgez $s2,%s\n", floorDone));
        emitText(String.format("\taddi %s,%s,-1\n", r(dst), r(dst)));
        emitText(String.format("%s:\n", floorDone));
        // Saturation clamping to [-2^15, 2^15-1]
        emitText(String.format("\tmove $s0,%s\n", r(dst)));
        emitText("\tmove $s3, $ra\n");
        emitText("\tjal saturate_arith\n");
        emitText("\tmove $ra, $s3\n");
        emitText(String.format("\tmove %s,$s0\n", r(dst)));
    }

    public void saturatingAdd(Temp dst, Temp t1, Temp t2) { add(dst, t1, t2); }
    public void saturatingSub(Temp dst, Temp t1, Temp t2) { sub(dst, t1, t2); }
    public void saturatingMul(Temp dst, Temp t1, Temp t2) { mul(dst, t1, t2); }

    /***********************/
    /* Control flow        */
    /***********************/
    public void returnFunction(Temp retVal) {
        emitText(String.format("\tmove $v0,%s\n", r(retVal)));
        emitText("\tjr $ra\n");
    }

    public void blt(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tblt %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    public void bge(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tbge %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    public void bne(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tbne %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    public void beq(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tbeq %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    public void beqz(Temp oprnd1, String label) {
        emitText(String.format("\tbeq %s,$zero,%s\n", r(oprnd1), label));
    }

    public void bgt(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tbgt %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    public void ble(Temp oprnd1, Temp oprnd2, String label) {
        emitText(String.format("\tble %s,%s,%s\n", r(oprnd1), r(oprnd2), label));
    }

    /***********************/
    /* Calls               */
    /***********************/
    public void call(String funcName, Temp dst, Temp arg1, Temp arg2) {
        emitText(String.format("\tmove $a0,%s\n", r(arg1)));
        emitText(String.format("\tmove $a1,%s\n", r(arg2)));
        emitText(String.format("\tjal %s\n", funcName));
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    public void virtualCall(Temp obj, int methodOffsetBytes, List<Temp> args, Temp dst) {
        requireInvalidPtr();

        for (int i = args.size() - 1; i >= 0; i--) {
            push(args.get(i));
        }
        push(obj);

        beqz(obj, "invalid_ptr_dref");
        emitText(String.format("\tlw $s0, 0(%s)\n", r(obj)));
        emitText(String.format("\tlw $s1, %d($s0)\n", methodOffsetBytes));
        emitText("\tjalr $s1\n");

        popBytes(4 * (args.size() + 1));

        if (dst != null) {
            moveV0To(dst);
        }
    }

    /***********************/
    /* Objects / arrays    */
    /***********************/
    public void newArray(Temp dst, Temp len) {
        emitText(String.format("\tmove $s7, %s\n", r(len)));
        emitText("\tli $v0,9\n");
        emitText("\tmove $a0, $s7\n");
        emitText("\taddi $a0, $a0,1\n");
        emitText("\tmul $a0, $a0, 4\n");
        emitText("\tsyscall\n");
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
        emitText(String.format("\tsw $s7, 0(%s)\n", r(dst)));
    }

    public void arrayAccess(Temp dst, Temp array, Temp index) {
        requireInvalidPtr();
        requireAccessViolation();

        String dstReg = r(dst);
        String arrReg = r(array);
        String idxReg = r(index);

        String zeroReg = "$s0";
        String oneReg = "$s1";
        String fourReg = "$s2";
        String lenReg = "$s3";
        String idx1Reg = "$s4";
        String offBReg = "$s5";
        String addrReg = "$s6";

        emitText(String.format("\tli %s, 0\n", zeroReg));
        emitText(String.format("\tli %s, 1\n", oneReg));
        emitText(String.format("\tli %s, 4\n", fourReg));
        emitText(String.format("\tbeq %s, %s, invalid_ptr_dref\n", arrReg, zeroReg));
        emitText(String.format("\tblt %s, %s, access_violation\n", idxReg, zeroReg));
        emitText(String.format("\tlw %s, 0(%s)\n", lenReg, arrReg));
        emitText(String.format("\tbge %s, %s, access_violation\n", idxReg, lenReg));
        emitText(String.format("\tadd %s, %s, %s\n", idx1Reg, idxReg, oneReg));
        emitText(String.format("\tmul %s, %s, %s\n", offBReg, idx1Reg, fourReg));
        emitText(String.format("\tadd %s, %s, %s\n", addrReg, arrReg, offBReg));
        emitText(String.format("\tlw %s, 0(%s)\n", dstReg, addrReg));
    }

    public void arraySet(Temp array, Temp index, Temp value) {
        requireInvalidPtr();
        requireAccessViolation();

        String arrReg = r(array);
        String idxReg = r(index);
        String valReg = r(value);
        String lenReg = "$s0";
        String idx1Reg = "$s1";
        String offReg = "$s2";
        String addrReg = "$s3";

        emitText(String.format("\tbeq %s, $zero, invalid_ptr_dref\n", arrReg));
        emitText(String.format("\tblt %s, $zero, access_violation\n", idxReg));
        emitText(String.format("\tlw %s, 0(%s)\n", lenReg, arrReg));
        emitText(String.format("\tbge %s, %s, access_violation\n", idxReg, lenReg));
        emitText(String.format("\taddi %s, %s, 1\n", idx1Reg, idxReg));
        emitText(String.format("\tsll %s, %s, 2\n", offReg, idx1Reg));
        emitText(String.format("\tadd %s, %s, %s\n", addrReg, arrReg, offReg));
        emitText(String.format("\tsw %s, 0(%s)\n", valReg, addrReg));
    }

    public void fieldAccess(Temp dst, Temp obj, int offsetBytes) {
        requireInvalidPtr();
        beqz(obj, "invalid_ptr_dref");
        lw(dst, obj, offsetBytes);
    }

    public void fieldSet(Temp obj, int offsetBytes, Temp value) {
        requireInvalidPtr();
        beqz(obj, "invalid_ptr_dref");
        sw(value, obj, offsetBytes);
    }

    public void loadAccess(Temp dst, Temp ref, int offset) {
        requireInvalidPtr();
        emitText(String.format("\tbeq %s, $zero, invalid_ptr_dref\n", r(ref)));
        emitText(String.format("\tlw %s, %d(%s)\n", r(dst), offset, r(ref)));
    }

    public void initVtable(Temp obj, String className) {
        emitText(String.format("\tla $s0, vt_%s\n", className));
        emitText(String.format("\tsw $s0, 0(%s)\n", r(obj)));
    }

    /***********************/
    /* Strings             */
    /***********************/
    public void emitConcatStrings() {
        requireConcatStrings();
        requireInvalidPtr();
    }

    public void concatStrings(Temp dst, Temp t1, Temp t2) {
        requireConcatStrings();
        requireInvalidPtr();
        emitText(String.format("\tmove $a0,%s\n", r(t1)));
        emitText(String.format("\tmove $a1,%s\n", r(t2)));
        emitText("\tjal concat_strings\n");
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    public void emitStrEq() {
        requireStrEq();
        requireInvalidPtr();
    }

    public void strEq(Temp dst, Temp s1, Temp s2) {
        requireStrEq();
        requireInvalidPtr();
        emitText(String.format("\tmove $a0,%s\n", r(s1)));
        emitText(String.format("\tmove $a1,%s\n", r(s2)));
        emitText("\tjal str_eq\n");
        emitText(String.format("\tmove %s,$v0\n", r(dst)));
    }

    /***********************/
    /* Syscalls            */
    /***********************/
    public void printInt(Temp t) {
        emitText(String.format("\tmove $a0,%s\n", r(t)));
        emitText("\tli $v0,1\n");
        emitText("\tsyscall\n");
        emitText("\tli $a0,32\n");
        emitText("\tli $v0,11\n");
        emitText("\tsyscall\n");
    }

    public void printString(String label) {
        emitText(String.format("\tla $a0,%s\n", label));
        emitText("\tli $v0,4\n");
        emitText("\tsyscall\n");
    }

    public void printString(Temp addr) {
        emitText(String.format("\tmove $a0,%s\n", r(addr)));
        emitText("\tli $v0,4\n");
        emitText("\tsyscall\n");
    }

    public void emitProgramMain(String userMainLabel) {
        emitText("main:\n");
        emitText(String.format("\tjal %s\n", userMainLabel));
        emitText("\tli $v0,10\n");
        emitText("\tsyscall\n");
    }

    /***********************/
    /* Prologue / Epilogue */
    /***********************/
    public void functionPrologue() {
        emitText("\tsubu $sp, $sp, 4\n");
        emitText("\tsw $ra, 0($sp)\n");
        emitText("\tsubu $sp, $sp, 4\n");
        emitText("\tsw $fp, 0($sp)\n");
        emitText("\tmove $fp, $sp\n");

        for (int i = 0; i < SAVED_T_COUNT; i++) {
            emitText("\tsubu $sp, $sp, 4\n");
            emitText(String.format("\tsw $t%d, 0($sp)\n", i));
        }

        if (currentLocalsBytes > 0) {
            emitText(String.format("\tsubu $sp, $sp, %d\n", currentLocalsBytes));
        }
    }

    public void functionEpilogueAndReturn() {
        emitText("\tmove $sp, $fp\n");
        for (int i = 0; i < SAVED_T_COUNT; i++) {
            int off = -(WORD_SIZE * (i + 1));
            emitText(String.format("\tlw $t%d, %d($sp)\n", i, off));
        }
        emitText("\tlw $fp, 0($sp)\n");
        emitText("\tlw $ra, 4($sp)\n");
        emitText("\taddi $sp, $sp, 8\n");
        emitText("\tjr $ra\n");
    }

    /***********************/
    /* Runtime data        */
    /***********************/
    private void emitRuntimeDataOnce() {
        if (runtimeDataEmitted) return;
        runtimeDataEmitted = true;

        if (needAccessViolation) {
            emitData("string_access_violation: .asciiz \"Access Violation\"\n");
        }
        if (needDivByZero) {
            emitData("string_illegal_div_by_0: .asciiz \"Illegal Division By Zero\"\n");
        }
        if (needInvalidPtr) {
            emitData("string_invalid_ptr_dref: .asciiz \"Invalid Pointer Dereference\"\n");
        }
    }

    /***********************/
    /* Runtime helpers     */
    /***********************/
    private void emitRuntimeHelpersOnce() {
        if (runtimeHelpersEmitted) return;
        runtimeHelpersEmitted = true;

        if (needAccessViolation) {
            emitRuntime("access_violation:\n");
            emitRuntime("\tla $a0,string_access_violation\n");
            emitRuntime("\tli $v0,4\n");
            emitRuntime("\tsyscall\n");
            emitRuntime("\tli $v0,10\n");
            emitRuntime("\tsyscall\n\n");
        }

        if (needDivByZero) {
            emitRuntime("div_by_zero_error:\n");
            emitRuntime("\tla $a0,string_illegal_div_by_0\n");
            emitRuntime("\tli $v0,4\n");
            emitRuntime("\tsyscall\n");
            emitRuntime("\tli $v0,10\n");
            emitRuntime("\tsyscall\n\n");
        }

        if (needInvalidPtr) {
            emitRuntime("invalid_ptr_dref:\n");
            emitRuntime("\tla $a0,string_invalid_ptr_dref\n");
            emitRuntime("\tli $v0,4\n");
            emitRuntime("\tsyscall\n");
            emitRuntime("\tli $v0,10\n");
            emitRuntime("\tsyscall\n\n");
        }

        if (needConcatStrings) {
            emitConcatStringsBody();
        }

        if (needStrEq) {
            emitStrEqBody();
        }

        if (needSaturation) {
            emitSaturationBody();
        }
    }

    private void emitStrEqBody() {
    emitRuntime("str_eq:\n");

    emitRuntime("\tsubu $sp,$sp,8\n");
    emitRuntime("\tsw $t2,0($sp)\n");
    emitRuntime("\tsw $t3,4($sp)\n");

    emitRuntime("\tbeq $a0,$zero,invalid_ptr_dref\n");
    emitRuntime("\tbeq $a1,$zero,invalid_ptr_dref\n");
    emitRuntime("str_eq_loop:\n");
    emitRuntime("\tlb $t2,0($a0)\n");
    emitRuntime("\tlb $t3,0($a1)\n");
    emitRuntime("\tbne $t2,$t3,str_eq_false\n");
    emitRuntime("\tbeq $t2,$zero,str_eq_true\n");
    emitRuntime("\taddi $a0,$a0,1\n");
    emitRuntime("\taddi $a1,$a1,1\n");
    emitRuntime("\tj str_eq_loop\n");

    emitRuntime("str_eq_true:\n");
    emitRuntime("\tli $v0,1\n");
    emitRuntime("\tlw $t2,0($sp)\n");
    emitRuntime("\tlw $t3,4($sp)\n");
    emitRuntime("\taddi $sp,$sp,8\n");
    emitRuntime("\tjr $ra\n");

    emitRuntime("str_eq_false:\n");
    emitRuntime("\tli $v0,0\n");
    emitRuntime("\tlw $t2,0($sp)\n");
    emitRuntime("\tlw $t3,4($sp)\n");
    emitRuntime("\taddi $sp,$sp,8\n");
    emitRuntime("\tjr $ra\n\n");
}

    private void emitConcatStringsBody() {
    emitRuntime("concat_strings:\n");

    // preserve caller state that this helper clobbers
    emitRuntime("\tsubu $sp,$sp,36\n");
    emitRuntime("\tsw $t0,0($sp)\n");
    emitRuntime("\tsw $t1,4($sp)\n");
    emitRuntime("\tsw $t2,8($sp)\n");
    emitRuntime("\tsw $t3,12($sp)\n");
    emitRuntime("\tsw $t4,16($sp)\n");
    emitRuntime("\tsw $t5,20($sp)\n");
    emitRuntime("\tsw $t6,24($sp)\n");
    emitRuntime("\tsw $s0,28($sp)\n");
    emitRuntime("\tsw $s1,32($sp)\n");

    emitRuntime("\tbeq $a0,$zero,invalid_ptr_dref\n");
    emitRuntime("\tbeq $a1,$zero,invalid_ptr_dref\n");
    emitRuntime("\tmove $t5,$a0\n");
    emitRuntime("\tmove $t6,$a1\n");

    emitRuntime("\tmove $t0,$t5\n");
    emitRuntime("\tli $s0,0\n");
    emitRuntime("__len1_loop:\n");
    emitRuntime("\tlb $t1,0($t0)\n");
    emitRuntime("\tbeq $t1,$zero,__len1_done\n");
    emitRuntime("\taddi $s0,$s0,1\n");
    emitRuntime("\taddi $t0,$t0,1\n");
    emitRuntime("\tj __len1_loop\n");
    emitRuntime("__len1_done:\n");

    emitRuntime("\tmove $t0,$t6\n");
    emitRuntime("\tli $s1,0\n");
    emitRuntime("__len2_loop:\n");
    emitRuntime("\tlb $t1,0($t0)\n");
    emitRuntime("\tbeq $t1,$zero,__len2_done\n");
    emitRuntime("\taddi $s1,$s1,1\n");
    emitRuntime("\taddi $t0,$t0,1\n");
    emitRuntime("\tj __len2_loop\n");
    emitRuntime("__len2_done:\n");

    emitRuntime("\taddu $t2,$s0,$s1\n");
    emitRuntime("\taddi $t2,$t2,1\n");
    emitRuntime("\tmove $a0,$t2\n");
    emitRuntime("\tli $v0,9\n");
    emitRuntime("\tsyscall\n");
    emitRuntime("\tmove $t3,$v0\n");
    emitRuntime("\tmove $t4,$v0\n");

    emitRuntime("\tmove $t0,$t5\n");
    emitRuntime("__cpy1:\n");
    emitRuntime("\tlb $t1,0($t0)\n");
    emitRuntime("\tbeq $t1,$zero,__cpy1_done\n");
    emitRuntime("\tsb $t1,0($t3)\n");
    emitRuntime("\taddi $t0,$t0,1\n");
    emitRuntime("\taddi $t3,$t3,1\n");
    emitRuntime("\tj __cpy1\n");
    emitRuntime("__cpy1_done:\n");

    emitRuntime("\tmove $t0,$t6\n");
    emitRuntime("__cpy2:\n");
    emitRuntime("\tlb $t1,0($t0)\n");
    emitRuntime("\tbeq $t1,$zero,__cpy2_done\n");
    emitRuntime("\tsb $t1,0($t3)\n");
    emitRuntime("\taddi $t0,$t0,1\n");
    emitRuntime("\taddi $t3,$t3,1\n");
    emitRuntime("\tj __cpy2\n");
    emitRuntime("__cpy2_done:\n");

    emitRuntime("\tsb $zero,0($t3)\n");
    emitRuntime("\tmove $v0,$t4\n");

    // restore caller state
    emitRuntime("\tlw $t0,0($sp)\n");
    emitRuntime("\tlw $t1,4($sp)\n");
    emitRuntime("\tlw $t2,8($sp)\n");
    emitRuntime("\tlw $t3,12($sp)\n");
    emitRuntime("\tlw $t4,16($sp)\n");
    emitRuntime("\tlw $t5,20($sp)\n");
    emitRuntime("\tlw $t6,24($sp)\n");
    emitRuntime("\tlw $s0,28($sp)\n");
    emitRuntime("\tlw $s1,32($sp)\n");
    emitRuntime("\taddi $sp,$sp,36\n");

    emitRuntime("\tjr $ra\n\n");
}

    private void emitSaturationBody() {
        emitRuntime("saturate_arith:\n");
        emitRuntime("\tli $s1,32767\n");
        emitRuntime("\tbgt $s0,$s1,overflow_high\n");
        emitRuntime("\tli $s1,-32768\n");
        emitRuntime("\tblt $s0,$s1,overflow_low\n");
        emitRuntime("\tjr $ra\n\n");
        emitRuntime("overflow_high:\n");
        emitRuntime("\tli $s0,32767\n");
        emitRuntime("\tjr $ra\n\n");
        emitRuntime("overflow_low:\n");
        emitRuntime("\tli $s0,-32768\n");
        emitRuntime("\tjr $ra\n\n");
    }
	public void emitExit()
{
    emitText("\tli $v0,10\n");
    emitText("\tsyscall\n");
}

    /***********************/
    /* Final output        */
    /***********************/
    public void finalizeFile() {
        emitRuntimeDataOnce();
        emitRuntimeHelpersOnce();

        fileWriter.println(".data");
        fileWriter.print(dataBuffer.toString());
        fileWriter.println();

        fileWriter.println(".text");
        fileWriter.print(runtimeBuffer.toString());
        fileWriter.print(textBuffer.toString());
        fileWriter.close();
    }
}

