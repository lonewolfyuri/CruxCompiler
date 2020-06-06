package crux.frontend.ast;

import java.util.List;

public abstract class ListNode<T extends Node> extends BaseNode {
    protected final List<T> children;

    ListNode(Position position, List<T> children) {
        super(position);
        this.children = children;
    }

    @Override
    public List<Node> getChildren() {
        return List.copyOf(children);
    }
}
