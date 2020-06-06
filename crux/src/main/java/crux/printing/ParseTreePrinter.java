package crux.printing;

import crux.frontend.pt.CruxParser;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.PrintStream;

public final class ParseTreePrinter {
    private static final String indent = "  ";

    private final PrintStream stdout;
    private int level = 0;

    public ParseTreePrinter(PrintStream stdout) {
        this.stdout = stdout;
    }

    public void print(ParserRuleContext ctx) {
        var ruleName = CruxParser.ruleNames[ctx.getRuleIndex()];
        stdout.printf("%s%s%n", indent.repeat(level), ruleName);

        if (ctx.children != null) {
            ++level;
            for (var child : ctx.children) {
                if (child instanceof ParserRuleContext)
                    print((ParserRuleContext) child);
            }
            --level;
        }
    }
}
