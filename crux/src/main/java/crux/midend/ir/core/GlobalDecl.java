package crux.midend.ir.core;

import java.util.List;
import java.util.function.Function;

/**
 * Allocates a chunk of memory, either of a global variable or of an array (global and local).
 * <p>
 * Operation (pseudo-code):
 * <pre>
 * {@code
 * if (global)
 *     destVar = allocateInDataSection(numElement)
 * else
 *     destVar = reserveStackMemory(numElement)
 * }
 * </pre>
 */
public final class GlobalDecl {
    AddressVar mDestVar;
    Constant mNumElement;
  
    public GlobalDecl(AddressVar destVar, Constant numElement) {
        mDestVar = destVar;
        mNumElement = numElement;
    }

    public AddressVar getAllocatedAddress() {
        return mDestVar;
    }

    public Constant getNumElement() {
        return mNumElement;
    }

    public String format(Function<Value, String> valueFormatter) {
        var destVar = valueFormatter.apply(mDestVar);
        var typeStr = getAllocatedAddress().getType().toString();
        var numElement = valueFormatter.apply(getNumElement());
        return String.format("%s = allocate %s, %s", destVar, typeStr, numElement);
    }
}
