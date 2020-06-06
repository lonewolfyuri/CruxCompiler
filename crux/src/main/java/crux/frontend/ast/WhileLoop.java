package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class WhileLoop extends BaseNode implements Statement {
    private final Expression condition;
    private final StatementList body;

    public WhileLoop(Position position, Expression condition, StatementList body) {
        super(position);
        this.condition = condition;
        this.body = body;
    }

    public Expression getCondition() {
        return condition;
    }

    public StatementList getBody() {
        return body;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(condition, body);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
