package crux.frontend.types;

import crux.frontend.Symbol;
import crux.frontend.ast.*;
import crux.frontend.ast.traversal.NullNodeVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class TypeChecker {
    private final HashMap<Node, Type> typeMap = new HashMap<>();
    private final ArrayList<String> errors = new ArrayList<>();

    public ArrayList<String> getErrors() {
        return errors;
    }

    public void check(DeclarationList ast) {
        var inferenceVisitor = new TypeInferenceVisitor();
        inferenceVisitor.visit(ast);
    }

    private void addTypeError(Node n, String message) {
        errors.add(String.format("TypeError%s[%s]", n.getPosition(), message));
    }

    private void setNodeType(Node n, Type ty) {
        typeMap.put(n, ty);
        if (ty.getClass() == ErrorType.class) {
            var error = (ErrorType) ty;
            addTypeError(n, error.getMessage());
        }
    }

    /**
     *  Returns type of given AST Node.
     */

    public Type getType(Node n) {
        return typeMap.get(n);
    }

    private final class TypeInferenceVisitor extends NullNodeVisitor {
        private Symbol currentFunctionSymbol;
        private Type currentFunctionReturnType;

        private boolean lastStatementReturns;

        private HashMap<String, Boolean> returns;

        @Override
        public void visit(Name name) {
            setNodeType(name, new AddressType(name.getSymbol().getType()));
        }

        @Override
        public void visit(ArrayDeclaration arrayDeclaration) {
            ArrayType arr = (ArrayType) arrayDeclaration.getSymbol().getType();
            if (arr.getBase().getClass() == IntType.class || arr.getBase().getClass() == BoolType.class) setNodeType(arrayDeclaration, arr);
            else setNodeType(arrayDeclaration, new ErrorType(String.format("Array %s has invalid base type %s.", arrayDeclaration.getSymbol().getName(), arr.getBase().toString())));
        }

        @Override
        public void visit(Assignment assignment) {
            assignment.getValue().accept(this);
            assignment.getLocation().accept(this);
            setNodeType(assignment, getType(assignment.getLocation()).assign(getType(assignment.getValue())));
        }

        @Override
        public void visit(Call call) {
            TypeList calls = new TypeList();
            for (Expression expr : call.getArguments()) {
                expr.accept(this);
                if (getType(expr).getClass() == AddressType.class) calls.append(((AddressType) getType(expr)).getBaseType());
                else calls.append(getType(expr));
            }
            setNodeType(call, call.getCallee().getType().call(calls));
        }

        @Override
        public void visit(DeclarationList declarationList) {
            for (Node declaration : declarationList.getChildren()) declaration.accept(this);
        }

        @Override
        public void visit(Dereference dereference) {
            dereference.getAddress().accept(this);
            setNodeType(dereference, getType(dereference.getAddress()).deref());
        }

        private boolean statementReturns(Node statement) {
            if (statement.getClass() == StatementList.class) {
                boolean containsReturn = false;
                for (Node curStmt : statement.getChildren()) containsReturn = containsReturn || statementReturns(curStmt);
                return containsReturn;
            } else if (statement.getClass() == IfElseBranch.class) {
                boolean thenBlock = statementReturns(((IfElseBranch) statement).getThenBlock());
                if (((IfElseBranch) statement).getElseBlock().getChildren().size() > 0) return (thenBlock && statementReturns(((IfElseBranch) statement).getElseBlock()));
                else return false;
            } else if (statement.getClass() == Return.class) return true;
            return false;
        }

        @Override
        public void visit(FunctionDefinition functionDefinition) {
            TypeList types = new TypeList();
            for (int i = 0; i < functionDefinition.getParameters().size(); i++) {
                Symbol sym = functionDefinition.getParameters().get(i);
                types.append(sym.getType());
                if (sym.getType().getClass() == ErrorType.class) setNodeType(functionDefinition, new ErrorType(String.format("Function %s has an error in argument in position %s: Unknown type: %s.", functionDefinition.getSymbol().getName(), i, ((ErrorType) sym.getType()).getMessage())));
                else if (sym.getType().getClass() == VoidType.class) setNodeType(functionDefinition, new ErrorType(String.format("Function %s has a void argument in position %s.", functionDefinition.getSymbol().getName(), i)));
            }

            boolean containsReturn = false;
            for (Node statement : functionDefinition.getStatements().getChildren()) {
                containsReturn = containsReturn || statementReturns(statement);
                lastStatementReturns = false;
                statement.accept(this);
                if ((lastStatementReturns) && (currentFunctionReturnType.getClass() != ((FuncType) functionDefinition.getSymbol().getType()).getRet().getClass())) setNodeType(statement, new ErrorType(String.format("Function %s returns %s not %s.", functionDefinition.getSymbol().getName(), ((FuncType) functionDefinition.getSymbol().getType()).getRet().toString(), currentFunctionReturnType.toString())));
            }

            currentFunctionSymbol = functionDefinition.getSymbol();

            if (functionDefinition.getSymbol().getName().equals("main")) {
                if (((FuncType) functionDefinition.getSymbol().getType()).getRet().getClass() != VoidType.class) setNodeType(functionDefinition, new ErrorType("Function main has invalid signature."));
                else if (functionDefinition.getParameters().size() > 0) setNodeType(functionDefinition, new ErrorType("Function main has invalid signature."));
                else setNodeType(functionDefinition, new FuncType(types, ((FuncType) functionDefinition.getSymbol().getType())).getRet());
            } else if ((!containsReturn) && (((FuncType) functionDefinition.getSymbol().getType()).getRet().getClass() != VoidType.class)) setNodeType(functionDefinition, new ErrorType(String.format("Not all code paths in function %s return a value.", functionDefinition.getSymbol().getName())));
            else setNodeType(functionDefinition, new FuncType(types, ((FuncType) functionDefinition.getSymbol().getType())).getRet());
        }

        @Override
        public void visit(IfElseBranch ifElseBranch) {
            ifElseBranch.getCondition().accept(this);
            if (getType(ifElseBranch.getCondition()).getClass() == BoolType.class) {
                //lastStatementReturns = false;
                ifElseBranch.getThenBlock().accept(this);
                if (ifElseBranch.getElseBlock().getChildren().size() > 0) {
                    //lastStatementReturns = false;
                    ifElseBranch.getElseBlock().accept(this);
                }
            } else setNodeType(ifElseBranch, new ErrorType(String.format("IfElseBranch requires bool condition not %s.", getType(ifElseBranch.getCondition()).toString())));
        }

        @Override
        public void visit(ArrayAccess access) {
            access.getOffset().accept(this);
            setNodeType(access, access.getBase().getSymbol().getType().index(getType(access.getOffset())));
        }

        @Override
        public void visit(LiteralBool literalBool) {
            setNodeType(literalBool, new BoolType());
        }

        @Override
        public void visit(LiteralInt literalInt) {
            setNodeType(literalInt, new IntType());
        }

        @Override
        public void visit(OpExpr op) {
            op.getLeft().accept(this);
            if (op.getRight() != null) op.getRight().accept(this);

            if (op.getOp().equals(OpExpr.Operation.ADD)) setNodeType(op, getType(op.getLeft()).add(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.SUB)) setNodeType(op, getType(op.getLeft()).sub(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.MULT)) setNodeType(op, getType(op.getLeft()).mul(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.DIV)) setNodeType(op, getType(op.getLeft()).div(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.LOGIC_AND)) setNodeType(op, getType(op.getLeft()).and(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.LOGIC_OR)) setNodeType(op, getType(op.getLeft()).or(getType(op.getRight())));
            else if (op.getOp().equals(OpExpr.Operation.LOGIC_NOT)) setNodeType(op, getType(op.getLeft()).not());
            else setNodeType(op, getType(op.getLeft()).compare(getType(op.getRight())));
        }

        @Override
        public void visit(Return ret) {
            ret.getValue().accept(this);
            currentFunctionReturnType = getType(ret.getValue());
            lastStatementReturns = true;
        }

        @Override
        public void visit(StatementList statementList) {
            for (Node statement : statementList.getChildren()) statement.accept(this);
        }

        @Override
        public void visit(VariableDeclaration variableDeclaration) {
            if (variableDeclaration.getSymbol().getType().getClass() == IntType.class || variableDeclaration.getSymbol().getType().getClass() == BoolType.class) setNodeType(variableDeclaration, variableDeclaration.getSymbol().getType());
            else setNodeType(variableDeclaration, new ErrorType(String.format("Variable %s has invalid type %s.", variableDeclaration.getSymbol().getName(), variableDeclaration.getSymbol().getType().toString())));
        }

        @Override
        public void visit(WhileLoop whileLoop) {
            whileLoop.getCondition().accept(this);
            if (getType(whileLoop.getCondition()).getClass() == BoolType.class) whileLoop.getBody().accept(this);
            else setNodeType(whileLoop, new ErrorType(String.format("WhileLoop requires bool condition not %s.", getType(whileLoop.getCondition()).toString())));
        }
    }
}
