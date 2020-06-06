package crux.midend.ir.core.insts;

import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.LocalVar;
import crux.midend.ir.core.Value;

import java.util.List;
import java.util.function.Function;

/**
 * Copies the source in to the destination.
 * <p>
 * Operation (pseudo-code): {@code destVar = source}
 */
public final class CopyInst extends Instruction {
    public CopyInst(LocalVar destVar, Value source) {
        super(destVar, List.of(source));
    }

    public Value getSrcValue() {
        return mOperands.get(0);
    }

    public LocalVar getDstVar() {
        return (LocalVar) mDestVar;
    }
  
    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        var dest = valueFormatter.apply(mDestVar);
        var source = valueFormatter.apply(getSrcValue());
        return String.format("%s = %s", dest, source);
    }
}
