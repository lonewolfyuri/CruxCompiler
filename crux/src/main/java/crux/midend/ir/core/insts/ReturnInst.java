package crux.midend.ir.core.insts;

import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.LocalVar;
import crux.midend.ir.core.Value;

import java.util.List;
import java.util.function.Function;

/**
 * Store the return value and leave the current function, returning the control to the caller.
 * <p>
 * Operation (pseudo-code):
 * <pre>{@code
 * $ReturnRegister = retValue
 * releaseCurrentStackFrame()
 * return
 * }</pre>
 */
public final class ReturnInst extends Instruction {
    public ReturnInst(LocalVar retValue) {
        super(List.of(retValue));
    }

    public LocalVar getReturnValue() {
        return (LocalVar)mOperands.get(0);
    }

    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        return String.format("return %s", valueFormatter.apply(getReturnValue()));
    }
}
