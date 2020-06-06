package crux.frontend.ast;

import crux.frontend.ast.traversal.NodeVisitor;

import java.util.List;

public final class OpExpr extends BaseNode implements Expression {
  public static enum Operation {
    GE(">="),
    LE("<="),
    NE("!="),
    EQ("=="),
    GT(">"),
    LT("<"),
    ADD("+"),
    SUB("-"),
    MULT("*"),
    DIV("/"),
    LOGIC_AND("and"),
    LOGIC_OR("or"),
    LOGIC_NOT("not");
    private String op;
    Operation(String op) {
      this.op = op;
    }
    public String toString() {
      return op;
    }
  }

  private final Operation op;
  private final Expression left;
  private final Expression right;

  public OpExpr(Position position, Operation op, Expression left, Expression right) {
    super(position);
    this.op = op;
    this.left = left;
    this.right = right;
  }

  public Operation getOp() {
    return op;
  }

  public Expression getLeft() {
    return left;
  }

  public Expression getRight() {
    return right;
  }

  @Override
  public List<Node> getChildren() {
    if (right != null)
      return List.of(left, right);
    else
      return List.of(left);
  }
  
  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }
}
