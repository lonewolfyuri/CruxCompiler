package crux.midend.ir.core;

import crux.frontend.types.Type;

/**
 * A value is anything, that can be used as an operand to an instruction. The purpose of the type is primarily to
 * verify, that the generated IR is consistent and that no type errors were introduced during lowering.
 */
public abstract class Value {
    protected Type mType;

    protected Value(Type type) {
        mType = type;
    }

    public Type getType() {
        return mType;
    }
}
