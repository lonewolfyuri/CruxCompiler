package crux.frontend.ast;

import crux.frontend.Symbol;
import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;
import java.util.ArrayList;

public final class Call extends BaseNode implements Expression, Statement {
    private final Symbol callee;
    private final List<Expression> arguments;

    public Call(Position position, Symbol callee, List<Expression> arguments) {
        super(position);
        this.callee = callee;
        this.arguments = arguments;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<Node>(arguments);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

    public Symbol getCallee() {
        return callee;
    }
}
