package crux.frontend;

import crux.frontend.types.Type;

public final class Symbol {
    private final String name;
    private final Type type;
    private final String error;

    Symbol(String name) {
        this.name = name;
        this.type = null;
        this.error = null;
    }

    Symbol(String name, Type type) {
        this.name = name;
        this.type = type;
        this.error = null;
    }

    Symbol(String name, String error) {
        this.name = name;
        this.type = null;
        this.error = error;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        if (error != null) {
            return String.format("Symbol(%s:%s)", name, error);
        }
        return String.format("Symbol(%s:%s)", name, type);
    }

    public String toString(boolean includeType) {
        if (error != null) {
            return toString();
        }
        return includeType ? toString() : String.format("Symbol(%s)", name);
    }
}
