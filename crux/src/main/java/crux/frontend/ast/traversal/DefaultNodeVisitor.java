package crux.frontend.ast.traversal;

import crux.frontend.ast.*;

public abstract class DefaultNodeVisitor implements NodeVisitor {
    protected abstract void visitDefault(Node node);

    @Override
    public void visit(ArrayAccess access) {
        visitDefault(access);
    }

    @Override
    public void visit(ArrayDeclaration arrayDeclaration) {
        visitDefault(arrayDeclaration);
    }

    @Override
    public void visit(Assignment assignment) {
        visitDefault(assignment);
    }

    @Override
    public void visit(Call call) {
        visitDefault(call);
    }

    @Override
    public void visit(DeclarationList declarationList) {
        visitDefault(declarationList);
    }

    @Override
    public void visit(Dereference dereference) {
        visitDefault(dereference);
    }

    @Override
    public void visit(FunctionDefinition functionDefinition) {
        visitDefault(functionDefinition);
    }

    @Override
    public void visit(IfElseBranch ifElseBranch) {
        visitDefault(ifElseBranch);
    }

    @Override
    public void visit(LiteralBool literalBool) {
        visitDefault(literalBool);
    }

    @Override
    public void visit(LiteralInt literalInt) {
        visitDefault(literalInt);
    }

    @Override
    public void visit(Name name) {
        visitDefault(name);
    }

    @Override
    public void visit(OpExpr op) {
        visitDefault(op);
    }

    @Override
    public void visit(Return ret) {
        visitDefault(ret);
    }

    @Override
    public void visit(StatementList statementList) {
        visitDefault(statementList);
    }

    @Override
    public void visit(VariableDeclaration variableDeclaration) {
        visitDefault(variableDeclaration);
    }

    @Override
    public void visit(WhileLoop whileLoop) {
        visitDefault(whileLoop);
    }
}
