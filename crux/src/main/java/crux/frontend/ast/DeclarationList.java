package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class DeclarationList extends ListNode<Declaration> {
    public DeclarationList(Position position, List<Declaration> declarations) {
        super(position, declarations);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
