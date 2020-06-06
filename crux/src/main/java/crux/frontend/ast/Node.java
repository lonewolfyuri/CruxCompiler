package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public interface Node {
    Position getPosition();

    List<Node> getChildren();

    void accept(NodeVisitor visitor);
}
