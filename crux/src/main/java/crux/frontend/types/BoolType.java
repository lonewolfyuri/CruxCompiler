package crux.frontend.types;

public final class BoolType extends Type {
    @Override
    public boolean equivalent(Type that) {
        return that.getClass() == BoolType.class;
    }

    @Override
    public String toString() {
        return "bool";
    }

    @Override
    public Type and(Type that) {
        if (that.toString().equals("bool")) {
            return new BoolType();
        } else {
            return super.and(that);
        }
    }

    @Override
    public Type or(Type that) {
        if (that.toString().equals("bool")) {
            return new BoolType();
        } else {
            return super.or(that);
        }
    }

    @Override
    public Type not() {
        return new BoolType();
    }

    @Override
    public Type deref() {
        return new BoolType();
    }

    @Override
    Type assign(Type source) {
        if (source.getClass() == BoolType.class) {
            return new BoolType();
        } else {
            return super.assign(source);
        }
    }
}