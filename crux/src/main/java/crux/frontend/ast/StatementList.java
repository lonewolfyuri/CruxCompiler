package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class StatementList extends ListNode<Statement> {
    public StatementList(Position position, List<Statement> statements) {
        super(position, statements);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
