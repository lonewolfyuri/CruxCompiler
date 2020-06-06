package crux.midend.ir.core.insts;

import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.Value;

import java.util.List;
import java.util.function.Function;

/**
 * Does nothing. Can be useful during lowering from AST to IR when a dummy instruction is needed.
 */
public final class NopInst extends Instruction {
    public NopInst() {
        super(List.of());
    }

    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        return "nop";
    }
}
