package crux.frontend;

import crux.frontend.ast.*;
import crux.frontend.ast.OpExpr.Operation;
import crux.frontend.pt.CruxBaseVisitor;
import crux.frontend.pt.CruxParser;
import crux.frontend.types.*;
import org.antlr.v4.runtime.ParserRuleContext;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * In this class, you're going to implement functionality that transform a input ParseTree
 * into an AST tree.
 *
 * The lowering process would start with {@link #lower(CruxParser.ProgramContext)}. Which take top-level
 * parse tree as input and process its children, function definitions and array declarations for example,
 * using other utilities functions or classes, like {@link \#lower(CruxParser.StatementListContext)} or {@link DeclarationVisitor},
 * recursively.
 * */
public final class ParseTreeLower {
    private final DeclarationVisitor declarationVisitor = new DeclarationVisitor();
    private final StatementVisitor statementVisitor = new StatementVisitor();
    private final ExpressionVisitor expressionVisitor = new ExpressionVisitor(true);
    private final ExpressionVisitor locationVisitor = new ExpressionVisitor(false);

    private final SymbolTable symTab;

    public ParseTreeLower(PrintStream err) {
        symTab = new SymbolTable(err);
    }

    private static Position makePosition(ParserRuleContext ctx) {
        var start = ctx.start;
        return new Position(start.getLine(), start.getCharPositionInLine());
    }

    public Type getType(String name) {
        if (name.equals("int")) {
            return new IntType();
        } else if (name.equals("bool")) {
            return new BoolType();
        } else if (name.equals("void")) {
            return new VoidType();
        } else {
            return new ErrorType(name);
        }
    }

    /**
     * Should returns true if we have encountered an error.
     */

    public boolean hasEncounteredError() {
        return symTab.hasEncounteredError();
    }

    /**
     * Lower top-level parse tree to AST
     * @return a {@link DeclarationList} object representing the top-level AST.
     * */
    public DeclarationList lower(CruxParser.ProgramContext program) {
        List<Declaration> declarations = new ArrayList<Declaration>();
        for (CruxParser.DeclarationContext dec : program.declarationList().declaration()) declarations.add(dec.accept(declarationVisitor));
        return new DeclarationList(makePosition(program.declarationList()), declarations);
    }

    /**
     * Lower statement list by lower individual statement into AST.
     * @return a {@link StatementList} AST object.
     * */

    private StatementList lower(CruxParser.StatementListContext statementList) {
        List<Statement> statements = new ArrayList<>();
        for (CruxParser.StatementContext statement : statementList.statement()) statements.add(statement.accept(statementVisitor));
        return new StatementList(makePosition(statementList), statements);
    }

    /**
     * Similar to {@link #lower(CruxParser.StatementListContext)}, but handling symbol table
     * as well.
     * @return a {@link StatementList} AST object.
     * */

    private StatementList lower(CruxParser.StatementBlockContext statementBlock) {
        List<Statement> statements = new ArrayList<>();
        symTab.enter();
        for (CruxParser.StatementContext statement : statementBlock.statementList().statement()) statements.add(statement.accept(statementVisitor));
        symTab.exit();
        return new StatementList(makePosition(statementBlock.statementList()), statements);
    }

    /**
     * A parse tree visitor to create AST nodes derived from {@link Declaration}
     * */
    private final class DeclarationVisitor extends CruxBaseVisitor<Declaration> {
        /**
         * Visit a parse tree variable declaration and create an AST {@link VariableDeclaration}
         * @return an AST {@link VariableDeclaration}
         * */

        @Override
        public VariableDeclaration visitVariableDeclaration(CruxParser.VariableDeclarationContext ctx) {
            Position pos = makePosition(ctx);
            return new VariableDeclaration(pos, symTab.add(pos, ctx.Identifier().getText(), getType(ctx.type().Identifier().getText())));
        }

        /**
         * Visit a parse tree array declaration and create an AST {@link ArrayDeclaration}
         * @return an AST {@link ArrayDeclaration}
         * */

        @Override
        public Declaration visitArrayDeclaration(CruxParser.ArrayDeclarationContext ctx) {
            Position pos = makePosition(ctx);
            return new ArrayDeclaration(pos, symTab.add(pos, ctx.Identifier().getText(), new ArrayType(Long.parseLong(ctx.Integer().getText()), getType(ctx.type().Identifier().getText()))));
        }

        /**
         * Visit a parse tree function definition and create an AST {@link FunctionDefinition}
         * @return an AST {@link FunctionDefinition}
         * */

        @Override
        public Declaration visitFunctionDefinition(CruxParser.FunctionDefinitionContext ctx) {
            Position pos = makePosition(ctx);
            List<Symbol> params = new ArrayList<>();
            TypeList tL = new TypeList();
            for (CruxParser.ParameterContext param : ctx.parameterList().parameter()) {
                tL.append(getType(param.type().Identifier().getText()));
            }
            Symbol funcSym = symTab.add(pos, ctx.Identifier().getText(), new FuncType(tL, getType(ctx.type().Identifier().getText())));

            symTab.enter();
            for (CruxParser.ParameterContext param : ctx.parameterList().parameter()) params.add(symTab.add(makePosition(param), param.Identifier().getText(), getType(param.type().Identifier().getText())));
            FunctionDefinition newFunc = new FunctionDefinition(pos, funcSym, params, lower(ctx.statementBlock().statementList()));
            symTab.exit();

            return newFunc;
        }
    }

    /**
     * A parse tree visitor to create AST nodes derived from {@link Statement}
     * */
    private final class StatementVisitor extends CruxBaseVisitor<Statement> {
        /**
         * Visit a parse tree variable declaration and create an AST {@link VariableDeclaration}.
         * Since {@link VariableDeclaration} is both {@link Declaration} and {@link Statement},
         * we simply delegate this to {@link DeclarationVisitor#visitArrayDeclaration(CruxParser.ArrayDeclarationContext)}
         * which we implement earlier.
         * @return an AST {@link VariableDeclaration}
         * */

        @Override
        public Statement visitVariableDeclaration(CruxParser.VariableDeclarationContext ctx) {
            return declarationVisitor.visitVariableDeclaration(ctx);
        }

        /**
         * Visit a parse tree assignment statement and create an AST {@link Assignment}
         * @return an AST {@link Assignment}
         * */

        @Override
        public Statement visitAssignmentStatement(CruxParser.AssignmentStatementContext ctx) {
            //return new Assignment(makePosition(ctx), new Dereference(makePosition(ctx.designator()), ctx.designator().accept(locationVisitor)), ctx.expression0().accept(expressionVisitor));
            return new Assignment(makePosition(ctx), ctx.designator().accept(locationVisitor), ctx.expression0().accept(expressionVisitor));
        }

        /**
         * Visit a parse tree call statement and create an AST {@link Call}.
         * Since {@link Call} is both {@link Expression} and {@link Statement},
         * we simply delegate this to {@link ExpressionVisitor#visitCallExpression(CruxParser.CallExpressionContext)}
         * that we will implement later.
         * @return an AST {@link Call}
         * */

        @Override
        public Statement visitCallStatement(CruxParser.CallStatementContext ctx) {
            return expressionVisitor.visitCallExpression(ctx.callExpression());
        }

        /**
         * Visit a parse tree if-else branch and create an AST {@link IfElseBranch}.
         * The template code shows partial implementations that visit the then block and else block
         * recursively before using those returned AST nodes to construct {@link IfElseBranch} object.
         * @return an AST {@link IfElseBranch}
         * */

        @Override
        public Statement visitIfStatement(CruxParser.IfStatementContext ctx) {
            if (ctx.statementBlock().size() > 1) return new IfElseBranch(makePosition(ctx), ctx.expression0().accept(expressionVisitor), lower(ctx.statementBlock(0)), lower(ctx.statementBlock(1)));
            else return new IfElseBranch(makePosition(ctx), ctx.expression0().accept(expressionVisitor), lower(ctx.statementBlock(0)), new StatementList(makePosition(ctx), new ArrayList<>()));
        }

        /**
         * Visit a parse tree while loop and create an AST {@link WhileLoop}.
         * You'll going to use a similar techniques as {@link #visitIfStatement(CruxParser.IfStatementContext)}
         * to decompose this construction.
         * @return an AST {@link WhileLoop}
         * */

        @Override
        public Statement visitWhileStatement(CruxParser.WhileStatementContext ctx) {
            return new WhileLoop(makePosition(ctx), ctx.expression0().accept(expressionVisitor), lower(ctx.statementBlock()));
        }

        /**
         * Visit a parse tree return statement and create an AST {@link Return}.
         * Here we show a simple example of how to lower a simple parse tree construction.
         * @return an AST {@link Return}
         * */

        @Override
        public Statement visitReturnStatement(CruxParser.ReturnStatementContext ctx) {
            return new Return(makePosition(ctx), ctx.expression0().accept(expressionVisitor));
        }
    }

    private final class ExpressionVisitor extends CruxBaseVisitor<Expression> {
        private final boolean dereferenceDesignator;

        private ExpressionVisitor(boolean dereferenceDesignator) {
            this.dereferenceDesignator = dereferenceDesignator;
        }

        @Override
        public Expression visitExpression0(CruxParser.Expression0Context ctx) {
            List<CruxParser.Expression1Context> exprs = ctx.expression1();
            if (exprs.size() == 1) return exprs.get(0).accept(expressionVisitor);
            else {
                switch (ctx.op0(0).getText()) {
                    case ">=":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.GE, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                    case "<=":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.LE, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                    case "!=":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.NE, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                    case "==":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.EQ, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                    case ">":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.GT, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                    case "<":
                        return new OpExpr(makePosition(ctx.op0(0)), Operation.LT, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                }
            }

            return new OpExpr(makePosition(ctx), Operation.EQ, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
        }

        @Override
        public Expression visitExpression1(CruxParser.Expression1Context ctx) {
            List<CruxParser.Expression2Context> exprs = ctx.expression2();
            List<CruxParser.Op1Context> ops = ctx.op1();
            if (exprs.size() == 1) return exprs.get(0).accept(expressionVisitor);
            else {
                Expression curExpr = null;
                switch (ops.get(0).getText()) {
                    case "+":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.ADD, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                        break;
                    case "-":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.SUB, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                        break;
                    case "or":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.LOGIC_OR, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                }

                for (int ndx = 1; ndx < ops.size(); ndx++) {
                    switch (ops.get(ndx).getText()) {
                        case "+":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.ADD, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                            break;
                        case "-":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.SUB, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                            break;
                        case "or":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.LOGIC_OR, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                    }
                }

                return curExpr;
            }
        }

        @Override
        public Expression visitExpression2(CruxParser.Expression2Context ctx) {
            List<CruxParser.Expression3Context> exprs = ctx.expression3();
            List<CruxParser.Op2Context> ops = ctx.op2();
            if (exprs.size() == 1) return exprs.get(0).accept(expressionVisitor);
            else {
                Expression curExpr = null;
                switch (ops.get(0).getText()) {
                    case "*":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.MULT, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                        break;
                    case "/":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.DIV, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                        break;
                    case "and":
                        curExpr = new OpExpr(makePosition(ops.get(0)), Operation.LOGIC_AND, exprs.get(0).accept(expressionVisitor), exprs.get(1).accept(expressionVisitor));
                }

                for (int ndx = 1; ndx < ops.size(); ndx++) {
                    switch (ops.get(ndx).getText()) {
                        case "*":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.MULT, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                            break;
                        case "/":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.DIV, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                            break;
                        case "and":
                            curExpr = new OpExpr(makePosition(ops.get(ndx)), Operation.LOGIC_AND, curExpr, exprs.get(ndx + 1).accept(expressionVisitor));
                    }
                }

                return curExpr;
            }
        }

        @Override
        public Expression visitExpression3(CruxParser.Expression3Context ctx) {
            if (ctx.expression3() != null) return new OpExpr(makePosition(ctx), Operation.LOGIC_NOT, ctx.expression3().accept(expressionVisitor), null);
            else if (ctx.expression0() != null) return ctx.expression0().accept(expressionVisitor);
            else if (ctx.designator() != null) return ctx.designator().accept(expressionVisitor);
            else if (ctx.callExpression() != null) return ctx.callExpression().accept(expressionVisitor);
            else if (ctx.literal() != null) return ctx.literal().accept(expressionVisitor);
            else return ctx.accept(expressionVisitor);
        }

        @Override
        public Call visitCallExpression(CruxParser.CallExpressionContext ctx) {
            List<Expression> exprs = new ArrayList<>();
            for (CruxParser.Expression0Context expr : ctx.expressionList().expression0()) exprs.add(expr.accept(expressionVisitor));
            return new Call(makePosition(ctx), symTab.lookup(makePosition(ctx), ctx.Identifier().getText()), exprs);
        }

        @Override
        public Expression visitDesignator(CruxParser.DesignatorContext ctx) {
            // TODO
            if (dereferenceDesignator) {
                if (ctx.expression0().size() > 0) return new Dereference(makePosition(ctx), new ArrayAccess(makePosition(ctx.expression0().get(0)), new Name(makePosition(ctx), symTab.lookup(makePosition(ctx), ctx.Identifier().getText())), ctx.expression0(0).accept(expressionVisitor)));
                else return new Dereference(makePosition(ctx), new Name(makePosition(ctx), symTab.lookup(makePosition(ctx), ctx.Identifier().getText())));
            } else {
                if (ctx.expression0().size() > 0) return new ArrayAccess(makePosition(ctx.expression0().get(0)), new Name(makePosition(ctx), symTab.lookup(makePosition(ctx), ctx.Identifier().getText())), ctx.expression0(0).accept(expressionVisitor));
                else return new Name(makePosition(ctx), symTab.lookup(makePosition(ctx), ctx.Identifier().getText()));
            }
        }

        @Override
        public Expression visitLiteral(CruxParser.LiteralContext ctx) {
            // TODO
            switch (ctx.getText()) {
                case "true":
                    return new LiteralBool(makePosition(ctx), true);
                case "false":
                    return new LiteralBool(makePosition(ctx), false);
                default:
                    return new LiteralInt(makePosition(ctx), Long.parseLong(ctx.getText()));
            }
        }
    }
}

