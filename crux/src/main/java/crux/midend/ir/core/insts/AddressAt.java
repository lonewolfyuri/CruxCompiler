package crux.midend.ir.core.insts;

import crux.midend.ir.core.AddressVar;
import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.Value;
import crux.midend.ir.core.Variable;
import crux.midend.ir.core.LocalVar;

import java.util.List;
import java.util.function.Function;

/**
 * Calculates the address given a base address and an offset.
 * <p>
 * Operation (pseudo-code): {@code destVar = base + offset}
 */
public final class AddressAt extends Instruction {
    public AddressAt(Variable destVar, AddressVar base, LocalVar offset) {
        super(destVar, List.of(base, offset));
    }

    public AddressAt(AddressVar destVar, AddressVar base) {
        super(destVar, List.of(base));
    }

    public AddressVar getBase() { return (AddressVar) mOperands.get(0); }

    public LocalVar getOffset() { return mOperands.size() > 1 ? (LocalVar)mOperands.get(1) : null; }

    public  AddressVar getDst() {
        return (AddressVar) mDestVar;
    }

    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        var dest = valueFormatter.apply(mDestVar);
        var base = valueFormatter.apply(getBase());
        var offset = valueFormatter.apply(getOffset());
        return String.format("%s = addressAt %s, %s", dest, base, offset);
    }
}
