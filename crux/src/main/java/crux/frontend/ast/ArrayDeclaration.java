package crux.frontend.ast;

import crux.frontend.Symbol;
import crux.frontend.ast.traversal.NodeVisitor;

public final class ArrayDeclaration extends BaseNode implements Declaration, Statement {
    private final Symbol symbol;

    public ArrayDeclaration(Position position, Symbol symbol) {
        super(position);
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
