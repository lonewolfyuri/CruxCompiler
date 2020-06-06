package crux.frontend.types;

import java.util.stream.Stream;

public final class AddressType extends Type {
    private final Type base;

    public AddressType(Type base) {
        this.base = base;
    }

    public Type getBaseType() { return base; }

    @Override
    public boolean equivalent(Type that) {
        if (that.getClass() != AddressType.class)
            return false;

        var aType = (AddressType) that;
        return base.equivalent(aType.base);
    }

    @Override
    public String toString() {
        return "Address(" + base + ")";
    }

    @Override
    public Type deref() {
        if (this.base.getClass() == VoidType.class) {
            return super.deref();
        } else {
            return getBaseType();
        }
    }

    @Override
    public Type index(Type that) {
        if (that.getClass() == IntType.class) {
            return ((ArrayType) getBaseType()).getBase();
        } else {
            return super.index(that);
        }
    }

    @Override
    Type assign(Type source) {
        if (source.getClass() == this.base.getClass()) {
            return this.base;
        } else {
            return super.assign(source);
        }
    }
}