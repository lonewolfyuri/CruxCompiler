package crux.midend.ir.core.insts;

import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.Value;
import crux.midend.ir.core.LocalVar;

import java.util.List;
import java.util.function.Function;

/**
 * Compares two values with each other. The result of the operation is a boolean.
 * <p>
 * Operation (pseudo-code): {@code destVar = compare(predicate, lhs, rhs)}
 */
public final class CompareInst extends Instruction {
    public enum Predicate {
        GE, GT,
        LE, LT,
        EQ, NE
    }
    private Predicate mPredicate;

    public CompareInst(LocalVar destVar, Predicate predicate, LocalVar lhs, LocalVar rhs) {
        super(destVar, List.of(lhs, rhs));
        mPredicate = predicate;
    }

    public Predicate getPredicate() {
        return mPredicate;
    }

    public LocalVar getLeftOperand() {
      return (LocalVar) mOperands.get(0);
    }

    public LocalVar getRightOperand() {
      return (LocalVar) mOperands.get(1);
    }

    public LocalVar getDst() {
        return (LocalVar) mDestVar;
    }
  
    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        var destVar = valueFormatter.apply(mDestVar);
        var lhs = valueFormatter.apply(getLeftOperand());
        var rhs = valueFormatter.apply(getRightOperand());
        String predicateStr = "";
        switch (mPredicate) {
            case GE: predicateStr = ">="; break;
            case GT: predicateStr = ">"; break;
            case LE: predicateStr = "<="; break;
            case LT: predicateStr = "<"; break;
            case EQ: predicateStr = "=="; break;
            case NE: predicateStr = "!="; break;
        }
        return String.format("%s = %s %s %s", destVar, lhs, predicateStr, rhs);
    }
}
