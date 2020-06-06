package crux.frontend.types;

public final class VoidType extends Type {
    @Override
    public boolean equivalent(Type that) {
        return that.getClass() == VoidType.class;
    }

    @Override
    public String toString() {
        return "void";
    }
}
