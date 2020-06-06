package crux.midend.ir.core;

import crux.frontend.types.Type;

/**
 * An address variable is a variable, that contains an address to a location in memory. This can be an array, a global
 * variable or a function argument.
 */
public class AddressVar extends Variable {

    public AddressVar(Type type) {
        super(type);
    }

    public AddressVar(Type type, String name) {
        super(type, name);
        mName = String.format("%%%s", mName);
    }

    public String toString() {
        return mName;
    }
}
