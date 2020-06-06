package crux.printing;

import crux.midend.ir.core.*;

import java.util.function.Function;

public final class IRValueFormatter implements Function<Value, String> {
    private int mAnonymousTempVarCount;

    public IRValueFormatter() {
        mAnonymousTempVarCount = 0;
    }

    @Override
    public String apply(Value value) {
        if (value instanceof BooleanConstant) {
            var boolConst = (BooleanConstant) value;
            return boolConst.getValue() ? "true" : "false";
        } else if (value instanceof IntegerConstant) {
            var intConst = (IntegerConstant) value;
            return String.format("%d", intConst.getValue());
        } else if (value instanceof LocalVar) {
            var scalarVar = (LocalVar) value;
            if (scalarVar.getName().length() == 0) {
                // Assign a name
                return String.format("$t%d", mAnonymousTempVarCount++);
            } else {
                return scalarVar.getName();
            }
        } else if (value instanceof AddressVar) {
            var addressVar = (AddressVar) value;
            return addressVar.getName();
        }
        return null;
    }

    @Override
    public <V> Function<V, String> compose(Function<? super V, ? extends Value> before) {
        return (V val) -> apply(before.apply(val));
    }

    @Override
    public <V> Function<Value, V> andThen(Function<? super String, ? extends V> after) {
        return (Value v) -> after.apply(apply(v));
    }
}
