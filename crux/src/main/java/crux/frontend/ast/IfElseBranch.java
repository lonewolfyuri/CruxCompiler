package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class IfElseBranch extends BaseNode implements Statement {
    private final Expression condition;
    private final StatementList thenBlock;
    private final StatementList elseBlock;

    public IfElseBranch(Position position, Expression condition, StatementList thenBlock, StatementList elseBlock) {
        super(position);
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }

    public Expression getCondition() {
        return condition;
    }

    public StatementList getThenBlock() {
        return thenBlock;
    }

    public StatementList getElseBlock() {
        return elseBlock;
    }

    @Override
    public List<Node> getChildren() {
        return List.of(condition, thenBlock, elseBlock);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
