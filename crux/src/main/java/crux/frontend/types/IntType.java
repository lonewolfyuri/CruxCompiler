package crux.frontend.types;

public final class IntType extends Type {
    @Override
    public String toString() {
        return "int";
    }

    @Override
    public Type add(Type that) {
        if (that.toString().equals("int")) {
            return new IntType();
        } else {
            return super.add(that);
        }
    }

    @Override
    public Type sub(Type that) {
        if (that.toString().equals("int")) {
            return new IntType();
        } else {
            return super.sub(that);
        }
    }

    @Override
    public Type mul(Type that) {
        if (that.toString().equals("int")) {
            return new IntType();
        } else {
            return super.mul(that);
        }
    }

    @Override
    public Type div(Type that) {
        if (that.toString().equals("int")) {
            return new IntType();
        } else {
            return super.div(that);
        }
    }

    @Override
    public Type compare(Type that) {
        if (that.toString().equals("int")) {
            return new BoolType();
        } else {
            return super.compare(that);
        }
    }

    @Override
    public boolean equivalent(Type that) {
        return that.getClass() == IntType.class;
    }

    @Override
    public Type deref() {
        return new IntType();
    }

    @Override
    Type assign(Type source) {
        if (source.getClass() == IntType.class) {
            return new IntType();
        } else {
            return super.assign(source);
        }
    }
}
