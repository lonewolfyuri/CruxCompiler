package crux.frontend.ast.traversal;

import crux.frontend.ast.*;

public interface NodeVisitor {
    void visit(ArrayAccess arrayAccess);

    void visit(ArrayDeclaration arrayDeclaration);

    void visit(Assignment assignment);

    void visit(Call call);

    void visit(DeclarationList declarationList);

    void visit(Dereference dereference);

    void visit(FunctionDefinition functionDefinition);

    void visit(IfElseBranch ifElseBranch);

    void visit(LiteralBool literalBool);

    void visit(LiteralInt literalInt);

    void visit(Name name);

    void visit(OpExpr operation);

    void visit(Return ret);

    void visit(StatementList statementList);

    void visit(VariableDeclaration variableDeclaration);

    void visit(WhileLoop whileLoop);
}
