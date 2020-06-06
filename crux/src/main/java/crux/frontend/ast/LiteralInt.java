package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

public final class LiteralInt extends BaseNode implements Expression {
    private final long value;

    public LiteralInt(Position position, long value) {
        super(position);
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
