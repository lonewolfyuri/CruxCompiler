package crux.printing;

import crux.midend.ir.core.*;

import java.io.PrintStream;

public final class IRPrinter {
    private final PrintStream mStdOut;
    private IRValueFormatter mValueFormatter;

    public IRPrinter(PrintStream stdout) {
        mValueFormatter = new IRValueFormatter();
        mStdOut = stdout;
    }

    public void print(Program program) {
        var text = program.format(mValueFormatter);
        mStdOut.print(text);
    }
}
