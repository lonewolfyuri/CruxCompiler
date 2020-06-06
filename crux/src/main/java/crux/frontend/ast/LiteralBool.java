package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

public final class LiteralBool extends BaseNode implements Expression {
    private final boolean value;

    public LiteralBool(Position position, boolean value) {
        super(position);
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
