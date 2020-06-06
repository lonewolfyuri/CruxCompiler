package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class Dereference extends BaseNode implements Expression {
    private final Expression address;

    public Dereference(Position position, Expression address) {
        super(position);
        this.address = address;
    }

    public Expression getAddress() {
        return address;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(address);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
