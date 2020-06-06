package crux.frontend.ast;

import java.util.List;

public abstract class BaseNode implements Node {
    private final Position position;

    BaseNode(Position position) {
        this.position = position;
    }

    @Override
    public List<Node> getChildren() {
        return List.of();
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
