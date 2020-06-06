package crux.midend.ir;

import crux.midend.ir.core.Value;

import java.util.function.Function;

public interface Formattable {
    String format(Function<Value, String> valueFormatter);
}
