package crux.midend.ir.core.insts;

public abstract class InstVisitor {
    public void visit(AddressAt i) {}
    public void visit(BinaryOperator i) {}
    public void visit(CompareInst i) {}
    public void visit(CopyInst i) {}
    public void visit(JumpInst i) {}
    public void visit(LoadInst i) {}
    public void visit(NopInst i) {}
    public void visit(StoreInst i) {}
    public void visit(UnaryNotInst i) {}
    public void visit(CallInst i) {}
    public void visit(ReturnInst i) {}
}
