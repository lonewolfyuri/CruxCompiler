package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class Assignment extends BaseNode implements Statement {
    private final Expression location;
    private final Expression value;

    public Assignment(Position position, Expression location, Expression value) {
        super(position);
        this.location = location;
        this.value = value;
    }

    public Expression getLocation() {
        return location;
    }

    public Expression getValue() {
        return value;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(location, value);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
