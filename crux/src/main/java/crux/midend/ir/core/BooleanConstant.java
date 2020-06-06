package crux.midend.ir.core;

import crux.frontend.types.BoolType;

import java.util.HashMap;

/**
 * A constant boolean (i.e. true or false). This is equivalent to {@link
 * crux.frontend.ast.LiteralBool}.
 */
public final class BooleanConstant extends Constant {
    private boolean mValue;

    private BooleanConstant(Program ctx, boolean val) {
        super(new BoolType());
        mValue = val;
    }

    public boolean getValue() { return mValue; }

    public static BooleanConstant get(Program ctx, boolean value) {
        var currentMap = mBoolConstantPool.computeIfAbsent(ctx, p -> new HashMap<>());
        return currentMap.computeIfAbsent(value, p -> new BooleanConstant(ctx, value));
    }
}
