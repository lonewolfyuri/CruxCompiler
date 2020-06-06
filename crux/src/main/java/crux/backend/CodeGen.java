package crux.backend;

import crux.frontend.ast.ArrayAccess;
import crux.frontend.types.ArrayType;
import crux.frontend.types.FuncType;
import crux.frontend.types.Type;
import crux.frontend.types.VoidType;
import crux.midend.ir.core.*;
import crux.midend.ir.core.insts.*;
import crux.printing.IRValueFormatter;

import java.util.*;

import static java.lang.Integer.max;

public final class CodeGen extends InstVisitor {
    private final IRValueFormatter irFormat = new IRValueFormatter();

    private final Program p;
    private final CodePrinter out;

    private Map<Instruction, String> mCurrentLabels = null;
    private Map<Variable, Integer> mStackMap = null, prevStackMap = null;
    private ArrayList<String> argRegs = new ArrayList<>(List.of("%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9"));
    private String reg1 = "%r10", reg2 = "%r11", returnReg = "%rax", backupReg = "%r12";

    public CodeGen(Program p) {
        this.p = p;
        // Do not change the file name that is outputted or it will
        // break the grader!
        out = new CodePrinter("a.s");
    }

    /* +=========================================================================================+
       |                                    Code Gen Methods                                     |
       +=========================================================================================+ */

    public void genCode() {
        //This function should generate code for the entire program.
        for (Iterator<GlobalDecl> it = p.getGlobals(); it.hasNext();) genGlobl(it.next());
        for (Iterator<Function> it = p.getFunctions(); it.hasNext();) genCode(it.next());
        out.close();
    }

    private void genCode(Function f) {
        updateStack();
        out.printCode(".globl " + f.getName());

        mCurrentLabels = assignLabels(f);
        List<LocalVar> args = f.getArguments();

        for (int ndx = 0; ndx < args.size(); ndx++) addVarToStack(args.get(ndx), ndx);
        out.printLabel(makeLabel(mCurrentLabels.get(f.getStart())));

        genCodeDFS(f.getStart());
        genEnter();
        out.outputBuffer();

        mCurrentLabels = null;
        resetStack();
    }

    /** Assigns Labels to any Instruction that might be the target of a
     * conditional or unconditional jump. */

    private HashMap<Instruction, String> assignLabels(Function f) {
        HashMap<Instruction, String> labelMap = new HashMap<>();
        Stack<Instruction> tovisit = new Stack<>();
        HashSet<Instruction> discovered = new HashSet<>();
        tovisit.push(f.getStart());
        while (!tovisit.isEmpty()) {
            Instruction inst = tovisit.pop();

            for (int childIdx = 0; childIdx < inst.numNext(); childIdx++) {
                Instruction child = inst.getNext(childIdx);
                if (discovered.contains(child)) {
                    //Found the node for a second time...need a label for merge points
                    if (!labelMap.containsKey(child)) {
                        labelMap.put(child, getNewLabel());
                    }
                } else {
                    discovered.add(child);
                    tovisit.push(child);
                    //Need a label for jump targets also
                    if (childIdx == 1 && !labelMap.containsKey(child)) {
                        labelMap.put(child, getNewLabel());
                    }
                }
            }
        }
        if (!labelMap.containsKey(f.getStart())) labelMap.put(f.getStart(), f.getName());
        return labelMap;
    }

    private void genCodeDFS(Instruction inst) {
        boolean firstRun = true;
        Stack<Instruction> frontier = new Stack<>();
        HashSet<Instruction> discovered = new HashSet<>();
        frontier.push(inst);
        while (!frontier.isEmpty()) {
            Instruction curInst = frontier.pop();
            if (!firstRun && mCurrentLabels.containsKey(curInst)) out.bufferLabel(makeLabel(mCurrentLabels.get(curInst)));
            curInst.accept(this);

            Instruction first = curInst.getNext(0), second = curInst.getNext(1);
            if (second != null && !discovered.contains(second)) {
                frontier.push(second);
                discovered.add(second);
            }
            if (first != null && !discovered.contains(first)) {
                frontier.push(first);
                discovered.add(first);
            } else if (curInst.getNext(0) != null && (frontier.isEmpty() || curInst.getNext(0) != frontier.peek())) out.bufferCode(makeUnCmd("jmp", mCurrentLabels.get(curInst.getNext(0))));
            else genLeave();
            firstRun = false;
        }
    }

    private void genGlobl(GlobalDecl decl) {
        out.printCode(".comm " + decl.getAllocatedAddress().getName().substring(1) + ", " + (((IntegerConstant) decl.getNumElement()).getValue() * 8) + ", 8");
    }

    private void genEnter() {
        out.printCode("enter $(8 * " + getSize()  + "), $0");
    }

    private void genLeave() {
        out.bufferCode("leave");
        out.bufferCode("ret");
    }

    /* +=========================================================================================+
       |                                 Stack/Label Methods                                     |
       +=========================================================================================+ */

    private int labelcount = 1;
    private String getNewLabel() {
        return "L" + (labelcount++);
    }

    private int stackcount = 1, prevcount = 1;
    private int numFree = 0, prevFree = 0;

    private void resetStack() {
        stackcount = prevcount;
        mStackMap = prevStackMap;
        numFree = prevFree;
    }

    private void updateStack() {
        prevcount = stackcount;
        stackcount = 1;
        prevStackMap = mStackMap;
        mStackMap = new HashMap<>();
        prevFree = numFree;
        numFree = 0;
    }

    private int getNewStack(int count) {
        if (numFree < count) {
            int newCount = count - numFree;
            if (newCount % 2 != 0) numFree += newCount + 1;
            else numFree += newCount;
        }

        numFree -= count;
        int result = stackcount;
        stackcount += count;
        return result;
    }

    private void addVarToStack(Variable var, int ndx) {
        int stackNdx;
        stackNdx = getNewStack(1);
        mStackMap.put(var, stackNdx);
        if (ndx < 6) out.bufferCode(makeBinCmd("movq", argRegs.get(ndx), getOffset(mStackMap.get(var))));
        else {
            out.bufferCode(makeBinCmd("movq", getAddress("%rbp", ((ndx - 4) * 8)), backupReg));
            out.bufferCode(makeBinCmd("movq", backupReg, getOffset(mStackMap.get(var))));
        }
    }

    private void addVarToStack(Variable var, String src) {
        int stackNdx;
        stackNdx = getNewStack(1);
        mStackMap.put(var, stackNdx);
        out.bufferCode(makeBinCmd("movq", src, getOffset(mStackMap.get(var))));
    }

    /* +=========================================================================================+
       |                                 String Builder Methods                                  |
       +=========================================================================================+ */

    private String makeLabel(String label) {
        return label + ":";
    }

    private String makeBinCmd(String cmd, String src, String dst) {
        return cmd + " " + src + ", " + dst;
    }

    private String makeUnCmd(String cmd, String target) {
        return cmd + " " + target;
    }

    /* +=========================================================================================+
       |                                     Utility Methods                                     |
       +=========================================================================================+ */

    private String getOffset(int stackNdx) {
        return (-8 * stackNdx) + "(%rbp)";
    }

    private String getAddress(String addrBase, int addrNdx) {
        return addrNdx + "(" + addrBase + ")";
    }

    private String getAddress(String addrBase) {
        return "0(" + addrBase + ")";
    }

    private int getSize() {
        int size = 0;
        for (Variable curVar : mStackMap.keySet()) {
            if (curVar.getType().getClass() == ArrayType.class) size += ((ArrayType) curVar.getType()).getExtent();
            else size++;
        }
        if (size % 2 == 0) return size;
        return size + 1;
    }

    /* +=========================================================================================+
       |                                     Visit Methods                                       |
       +=========================================================================================+ */

    public void visit(AddressAt i) {
        out.bufferCode("/* AddressAt */");
        String name = i.getBase().getName().substring(1);
        if (i.getOffset() != null) {
            out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getOffset())), reg2));
            out.bufferCode(makeBinCmd("movq", "$8", reg1));
            out.bufferCode(makeBinCmd("imul", reg1, reg2));
            out.bufferCode(makeBinCmd("movq", name + "@GOTPCREL(%rip)", reg1));
            out.bufferCode(makeBinCmd("addq", reg1, reg2));
        } else out.bufferCode(makeBinCmd("movq", name + "@GOTPCREL(%rip)", reg2));

        addVarToStack(i.getDst(), reg2);
    }

    public void visit(BinaryOperator i) {
        out.bufferCode("/* BinaryOperator */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getLeftOperand())), reg1));

        if (i.getOperator().equals(BinaryOperator.Op.Add)) out.bufferCode(makeBinCmd("addq", getOffset(mStackMap.get(i.getRightOperand())), reg1));
        else if (i.getOperator().equals(BinaryOperator.Op.Div)) out.bufferCode(makeBinCmd("idiv", getOffset(mStackMap.get(i.getRightOperand())), reg1));
        else if (i.getOperator().equals(BinaryOperator.Op.Mul)) out.bufferCode(makeBinCmd("imul", getOffset(mStackMap.get(i.getRightOperand())), reg1));
        else if (i.getOperator().equals(BinaryOperator.Op.Sub)) out.bufferCode(makeBinCmd("subq", getOffset(mStackMap.get(i.getRightOperand())), reg1));

        addVarToStack(i.getDst(), reg1);
    }

    public void visit(CompareInst i) {
        out.bufferCode("/* CompareInst */");
        out.bufferCode(makeBinCmd("movq", "$0", reg1));
        out.bufferCode(makeBinCmd("movq", "$1", returnReg));
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getLeftOperand())), reg2));
        out.bufferCode(makeBinCmd("cmp", getOffset(mStackMap.get(i.getRightOperand())), reg2));

        if (i.getPredicate().equals(CompareInst.Predicate.NE)) out.bufferCode(makeBinCmd("cmovne", returnReg, reg1));
        else if (i.getPredicate().equals(CompareInst.Predicate.LT)) out.bufferCode(makeBinCmd("cmovl", returnReg, reg1));
        else if (i.getPredicate().equals(CompareInst.Predicate.LE)) out.bufferCode(makeBinCmd("cmovle", returnReg, reg1));
        else if (i.getPredicate().equals(CompareInst.Predicate.GT)) out.bufferCode(makeBinCmd("cmovg", returnReg, reg1));
        else if (i.getPredicate().equals(CompareInst.Predicate.GE)) out.bufferCode(makeBinCmd("cmovge", returnReg, reg1));
        else out.bufferCode(makeBinCmd("cmove", returnReg, reg1));

        addVarToStack(i.getDst(), reg1);
    }

    public void visit(CopyInst i) {
        out.bufferCode("/* CopyInst */");
        Value srcVal = i.getSrcValue();
        Variable destVar = i.getDstVar();
        String src;
        if (srcVal.getClass() == BooleanConstant.class) {
            if (((BooleanConstant) srcVal).getValue()) src = "$1";
            else src = "$0";
        } else if (srcVal.getClass() == IntegerConstant.class) src = "$" + ((IntegerConstant) srcVal).getValue();
        else {
            out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(srcVal)), reg1));
            src = reg1;
        }
        if (!mStackMap.containsKey(destVar)) addVarToStack(destVar, src);
        else out.bufferCode(makeBinCmd("movq", src, getOffset(mStackMap.get(destVar))));
    }

    public void visit(JumpInst i) {
        out.bufferCode("/* JumpInst */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getPredicate())), reg1));
        out.bufferCode(makeBinCmd("cmp", "$1", reg1));
        out.bufferCode(makeUnCmd("je", mCurrentLabels.get(i.getNext(1))));
    }

    public void visit(LoadInst i) {
        out.bufferCode("/* LoadInst */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getSrcAddress())), reg1));
        out.bufferCode(makeBinCmd("movq", getAddress(reg1), reg1));
        addVarToStack(i.getDst(), reg1);
    }

    public void visit(NopInst i) {
        out.bufferCode("/* NopInst */");
    }

    public void visit(StoreInst i) {
        out.bufferCode("/* StoreInst */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getSrcValue())), reg1));

        if (mStackMap.containsKey(i.getDestAddress())) out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getDestAddress())), reg2));
        //else if (varInStack(i.getDestAddress().getName().substring(1))) out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(getVarWithName(i.getDestAddress().getName().substring(1)))), reg2));

        out.bufferCode(makeBinCmd("movq", reg1, getAddress(reg2)));
    }

    public void visit(ReturnInst i) {
        out.bufferCode("/* ReturnInst */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getReturnValue())), returnReg));
        genLeave();
    }

    public void visit(CallInst i) {
        out.bufferCode("/* CallInst */");
        List<Value> params = i.getParams();
        int argNdx = 2;
        for (int ndx = params.size() - 1; ndx >= 0; ndx--) {
            if (ndx < 6) out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(params.get(ndx))), argRegs.get(ndx)));
            else {
                out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(params.get(ndx))), reg1));
                addVarToStack(new LocalVar(params.get(ndx).getType()), reg1);
            }
        }
        out.bufferCode(makeUnCmd("call", i.getCallee().getName().substring(1)));
        if (((FuncType) i.getCallee().getType()).getRet().getClass() != VoidType.class) addVarToStack(i.getDst(), returnReg);
    }

    public void visit(UnaryNotInst i) {
        out.bufferCode("/* UnaryNotInst */");
        out.bufferCode(makeBinCmd("movq", getOffset(mStackMap.get(i.getInner())), reg1));
        out.bufferCode(makeUnCmd("not", reg1));
        out.bufferCode(makeBinCmd("movq", reg1, getOffset(mStackMap.get(i.getDst()))));
    }
}
