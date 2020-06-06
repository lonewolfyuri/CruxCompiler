package crux.midend.ir.core.insts;

import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.Value;
import crux.midend.ir.core.LocalVar;

import java.util.List;
import java.util.function.Function;

/**
 * Any binary expression operator.
 */
public final class BinaryOperator extends Instruction {
    public enum Op {
        Add,
        Sub,
        Mul,
        Div
    }
    protected Op mOp;
    public Op getOperator() { return mOp; }

    public BinaryOperator(Op op, LocalVar destVar, LocalVar lhsValue, LocalVar rhsValue) {
        super(destVar, List.of(lhsValue, rhsValue));
        mOp = op;
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
        String opStr = "";
        switch (getOperator()) {
            case Add: opStr = "+"; break;
            case Sub: opStr = "-"; break;
            case Mul: opStr = "*"; break;
            case Div: opStr = "/"; break;
        }
        var destVar = valueFormatter.apply(mDestVar);
        var lhs = valueFormatter.apply(mOperands.get(0));
        var rhs = valueFormatter.apply(mOperands.get(1));
        return String.format("%s = %s %s %s", destVar, lhs, opStr, rhs);
    }
}
