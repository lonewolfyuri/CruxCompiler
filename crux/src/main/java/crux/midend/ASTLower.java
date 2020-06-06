package crux.midend;

import crux.frontend.Symbol;
import crux.frontend.ast.*;
import crux.frontend.ast.traversal.NodeVisitor;
import crux.frontend.types.*;
import crux.midend.ir.core.*;
import crux.midend.ir.core.insts.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lower from AST to IR
 * */
public final class ASTLower implements NodeVisitor {
    private Program mCurrentProgram = null;
    private Function mCurrentFunction = null;

    private Map<Symbol, AddressVar> mCurrentGlobalSymMap = null;
    private Map<Symbol, Variable> mCurrentLocalVarMap = null;
    private Map<String, AddressVar> mBuiltInFuncMap = null;
    private TypeChecker mTypeChecker;
    private Value mExpressionValue = null;
    private Instruction mLastControlInstruction = null;

    private void addEdge(Instruction src, Instruction dst) {
        if (src == null) {
            mCurrentFunction.setStart(dst);
        } else {
            src.setNext(0, dst);
        }
    }

    public ASTLower(TypeChecker checker) {
        mTypeChecker = checker;
    }
  
    public Program lower(DeclarationList ast) {
        visit(ast);
        return mCurrentProgram;
    }

    /**
     * The top level Program
     * */
    private void initBuiltInFunctions() {
        // TODO: Add built-in function symbols
        mBuiltInFuncMap = new HashMap<>();
        mBuiltInFuncMap.put("readInt", new AddressVar(new FuncType(new TypeList(), new IntType()), "readInt"));
        ArrayList<Type> printBoolTypes = new ArrayList<Type>();
        printBoolTypes.add(new BoolType());
        mBuiltInFuncMap.put("printBool", new AddressVar(new FuncType(new TypeList(printBoolTypes), new VoidType()), "printBool"));
        ArrayList<Type> printIntTypes = new ArrayList<Type>();
        printIntTypes.add(new IntType());
        mBuiltInFuncMap.put("printInt", new AddressVar(new FuncType(new TypeList(printIntTypes), new VoidType()), "printInt"));
        mBuiltInFuncMap.put("println", new AddressVar(new FuncType(new TypeList(), new VoidType()), "println"));
    }

    @Override
    public void visit(DeclarationList declarationList) {
        initBuiltInFunctions();
        mCurrentProgram = new Program();
        mCurrentGlobalSymMap = new HashMap<>();
        for (Node declaration : declarationList.getChildren()) {
            declaration.accept(this);
        }
    }

    /**
     * Function
     * */
    @Override
    public void visit(FunctionDefinition functionDefinition) {
        mCurrentLocalVarMap = new HashMap<>();
        List<LocalVar> paramList = new ArrayList<LocalVar>();
        List<String> names = new ArrayList<>();
        for (Symbol sym : functionDefinition.getParameters()) {
            names.add(sym.getName());
        }
        int index = 0;
        for (Type param: ((FuncType) mTypeChecker.getType(functionDefinition)).getArgs()) {
            paramList.add(new LocalVar(param, names.get(index)));
            index++;
        }
        mCurrentFunction = new Function(functionDefinition.getSymbol().getName(), paramList, (FuncType) mTypeChecker.getType(functionDefinition));
        for (int i = 0; i < functionDefinition.getParameters().size(); i++) {
            mCurrentLocalVarMap.put(functionDefinition.getParameters().get(i), paramList.get(i));
        }
        mCurrentGlobalSymMap.put(functionDefinition.getSymbol(), new AddressVar(mTypeChecker.getType(functionDefinition), functionDefinition.getSymbol().getName()));
        functionDefinition.getStatements().accept(this);
        mCurrentProgram.addFunction(mCurrentFunction);
        mLastControlInstruction = null;
        mCurrentLocalVarMap = null;
        mCurrentFunction = null;
    }

    @Override
    public void visit(StatementList statementList) {
        for (int i = 0; i < statementList.getChildren().size(); i++) {
            statementList.getChildren().get(i).accept(this);
        }
    }

    /**
     * Declarations
     * */
    @Override
    public void visit(VariableDeclaration variableDeclaration) {
        if (mCurrentFunction == null) {
            var newAdr = new AddressVar(mTypeChecker.getType(variableDeclaration), variableDeclaration.getSymbol().getName());
            mCurrentGlobalSymMap.put(variableDeclaration.getSymbol(), newAdr);
            mCurrentProgram.addGlobalVar(new GlobalDecl(newAdr, IntegerConstant.get(mCurrentProgram, 1)));
        } else {
            var newAdr = new LocalVar(mTypeChecker.getType(variableDeclaration), variableDeclaration.getSymbol().getName());
            mCurrentLocalVarMap.put(variableDeclaration.getSymbol(), newAdr);
        }
    }
  
    @Override
    public void visit(ArrayDeclaration arrayDeclaration) {
        var newAdr = new AddressVar(mTypeChecker.getType(arrayDeclaration), arrayDeclaration.getSymbol().getName());
        mCurrentGlobalSymMap.put(arrayDeclaration.getSymbol(), newAdr);
        mCurrentProgram.addGlobalVar(new GlobalDecl(newAdr, IntegerConstant.get(mCurrentProgram, ((ArrayType) mTypeChecker.getType(arrayDeclaration)).getExtent())));
    }

    @Override
    public void visit(Name name) {
        Variable variable;
        if (mCurrentLocalVarMap.containsKey(name.getSymbol())) {
            variable = mCurrentLocalVarMap.get(name.getSymbol());
//            var newCpy = new CopyInst((LocalVar) variable, mExpressionValue);
//            addEdge(mLastControlInstruction, newCpy);
//            mLastControlInstruction = newCpy;
            mExpressionValue = variable;
        } else {
            variable = mCurrentGlobalSymMap.get(name.getSymbol());
            var newTmp = mCurrentFunction.getTempAddressVar(((AddressType) mTypeChecker.getType(name)).getBaseType());
            var newAdr = new AddressAt(newTmp, (AddressVar) variable);
            addEdge(mLastControlInstruction, newAdr);
            mLastControlInstruction = newAdr;
            mExpressionValue = newTmp;
        }

    }

    @Override
    public void visit(Assignment assignment) {
        assignment.getValue().accept(this);
        var value = mExpressionValue;
        assignment.getLocation().accept(this);
        var location = mExpressionValue;
        //var newTmp = mCurrentFunction.getTempAddressVar(mTypeChecker.getType(assignment));
        //var newAdr = new AddressAt(newTmp, (AddressVar) location);
        //addEdge(mLastControlInstruction, newAdr);
        //mLastControlInstruction = newAdr;
        if (location.getClass() != LocalVar.class) {
            var storeInst = new StoreInst((LocalVar) value, (AddressVar) location);
            addEdge(mLastControlInstruction, storeInst);
            mLastControlInstruction = storeInst;
        } else {
            var newCpy = new CopyInst((LocalVar) location, value);
            addEdge(mLastControlInstruction, newCpy);
            mLastControlInstruction = newCpy;
        }
    }

    @Override
    public void visit(Call call) {
        CallInst newCall;
        LocalVar tempVar;
        List<LocalVar> argList = new ArrayList<>();
        for (Expression expr : call.getArguments()) {
            expr.accept(this);
            argList.add((LocalVar) mExpressionValue);
        }
        if (mBuiltInFuncMap.containsKey(call.getCallee().getName())) {
            if (((FuncType) call.getCallee().getType()).getRet().getClass() == VoidType.class) {
                newCall = new CallInst(mBuiltInFuncMap.get(call.getCallee().getName()), argList);
            } else {
                tempVar = mCurrentFunction.getTempVar(mTypeChecker.getType(call));
                newCall = new CallInst(tempVar, mBuiltInFuncMap.get(call.getCallee().getName()), argList);
                mExpressionValue = tempVar;
            }
        } else {
            if (((FuncType) call.getCallee().getType()).getRet().getClass() == VoidType.class) {
                newCall = new CallInst(mCurrentGlobalSymMap.get(call.getCallee()), argList);
            } else {
                tempVar = mCurrentFunction.getTempVar(mTypeChecker.getType(call));
                newCall = new CallInst(tempVar, mCurrentGlobalSymMap.get(call.getCallee()), argList);
                mExpressionValue = tempVar;
            }
        }
        addEdge(mLastControlInstruction, newCall);
        mLastControlInstruction = newCall;
    }

    @Override
    public void visit(OpExpr operation) {
        operation.getLeft().accept(this);
        var leftVal = mExpressionValue;
        if (operation.getOp().toString().equals("and")) {
            var local = mCurrentFunction.getTempVar(mTypeChecker.getType(operation));
            var cpy = new CopyInst(local, leftVal);
            addEdge(mLastControlInstruction, cpy);
            mLastControlInstruction = cpy;
            var ji = new JumpInst(local);
            addEdge(mLastControlInstruction, ji);
            var newNop = new NopInst();
            ji.setNext(0, newNop);
            var newNop2 = new NopInst();
            ji.setNext(1, newNop2);
            mLastControlInstruction = newNop2;
            operation.getRight().accept(this);
            var newCpy = new CopyInst(local, mExpressionValue);
            addEdge(mLastControlInstruction, newCpy);
            mLastControlInstruction = newCpy;
            addEdge(mLastControlInstruction, newNop);
            mLastControlInstruction = newNop;
            mExpressionValue = local;
            return;
        }
        if (operation.getOp().toString().equals("or")) {
            var ji = new JumpInst((LocalVar) leftVal);
            addEdge(mLastControlInstruction, ji);
            var newNop = new NopInst();
            ji.setNext(0, newNop);
            var newNop2 = new NopInst();
            ji.setNext(1, newNop2);
            mLastControlInstruction = newNop;
            operation.getRight().accept(this);
            addEdge(mLastControlInstruction, newNop2);
            mLastControlInstruction = newNop2;
            return;
        }
        var destVar = mCurrentFunction.getTempVar(mTypeChecker.getType(operation));
        Instruction newInstruction = null;
        if (operation.getRight() == null) {
            newInstruction = new UnaryNotInst(destVar, (LocalVar) leftVal);
            addEdge(mLastControlInstruction, newInstruction);
            mLastControlInstruction = newInstruction;
            mExpressionValue = destVar;
            return;
        }
        operation.getRight().accept(this);
        var rightVal = mExpressionValue;
        if (operation.getOp().toString().equals("+")) {
            newInstruction = new BinaryOperator(BinaryOperator.Op.Add, destVar, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("-")) {
            newInstruction = new BinaryOperator(BinaryOperator.Op.Sub, destVar, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("*")) {
            newInstruction = new BinaryOperator(BinaryOperator.Op.Mul, destVar, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("/")) {
            newInstruction = new BinaryOperator(BinaryOperator.Op.Div, destVar, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals(">=")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.GE, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("<=")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.LE, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals(">")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.GT, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("<")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.LT, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("==")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.EQ, (LocalVar) leftVal, (LocalVar) rightVal);
        } else if (operation.getOp().toString().equals("!=")) {
            newInstruction = new CompareInst(destVar, CompareInst.Predicate.NE, (LocalVar) leftVal, (LocalVar) rightVal);
        }
        addEdge(mLastControlInstruction, newInstruction);
        mLastControlInstruction = newInstruction;
        mExpressionValue = destVar;
    }

    @Override
    public void visit(Dereference dereference) {
        dereference.getAddress().accept(this);
        var adrVar = mExpressionValue;
        if (adrVar.getClass() == LocalVar.class) {
            var dest = mCurrentFunction.getTempVar(mTypeChecker.getType(dereference));
            var newCpy = new CopyInst(dest, adrVar);
            addEdge(mLastControlInstruction, newCpy);
            mLastControlInstruction = newCpy;
            mExpressionValue = dest;
        } else {
            //var destVar = mCurrentFunction.getTempAddressVar(mTypeChecker.getType(dereference));
            //var newAdr = new AddressAt(destVar, (AddressVar) adrVar);
            //addEdge(mLastControlInstruction, newAdr);
            //mLastControlInstruction = newAdr;
            var tmpVar = mCurrentFunction.getTempVar(mTypeChecker.getType(dereference));
            var newLoad = new LoadInst(tmpVar, (AddressVar) adrVar);
            addEdge(mLastControlInstruction, newLoad);
            mLastControlInstruction = newLoad;
            mExpressionValue = tmpVar;
        }
    }

    private void visit(Expression expression) {
        expression.accept(this);
    }

    @Override
    public void visit(ArrayAccess access) {
        //access.getBase().accept(this);
        //var adr = mExpressionValue;
        access.getOffset().accept(this);
        var offset = mExpressionValue;
        var newTemp = mCurrentFunction.getTempAddressVar(mTypeChecker.getType(access));
        AddressVar adr = null;
        if (mCurrentLocalVarMap.containsKey(access.getBase().getSymbol())) {
            adr = (AddressVar) mCurrentLocalVarMap.get(access.getBase().getSymbol());
        } else {
            adr = (AddressVar) mCurrentGlobalSymMap.get(access.getBase().getSymbol());
        }
        var newAdr = new AddressAt(newTemp, (AddressVar) adr, (LocalVar) offset);
        addEdge(mLastControlInstruction, newAdr);
        mLastControlInstruction = newAdr;
        mExpressionValue = newTemp;
    }

    @Override
    public void visit(LiteralBool literalBool) {
        var boolValue = BooleanConstant.get(mCurrentProgram, literalBool.getValue());
        var destVar = mCurrentFunction.getTempVar(new BoolType());
        var copyInst = new CopyInst(destVar, boolValue);
        addEdge(mLastControlInstruction, copyInst);
        mLastControlInstruction = copyInst;
        mExpressionValue = destVar;
    }

    @Override
    public void visit(LiteralInt literalInt) {
        var intValue = IntegerConstant.get(mCurrentProgram,literalInt.getValue());
        var destVar = mCurrentFunction.getTempVar(new IntType());
        var copyInst = new CopyInst(destVar, intValue);
        addEdge(mLastControlInstruction, copyInst);
        mLastControlInstruction = copyInst;
        mExpressionValue = destVar;
    }

    @Override
    public void visit(Return ret) {
        ret.getValue().accept(this);
        var returnInst =  new ReturnInst((LocalVar) mExpressionValue);
        addEdge(mLastControlInstruction, returnInst);
        mLastControlInstruction = returnInst;
    }

    /**
     * Control Structures
     * */
    @Override
    public void visit(IfElseBranch ifElseBranch) {
        ifElseBranch.getCondition().accept(this);
        Instruction prevInstruction = mLastControlInstruction;
        JumpInst ji = new JumpInst((LocalVar) mExpressionValue);
        var newNop = new NopInst();
        ji.setNext(1, newNop);
        mLastControlInstruction = newNop;
        ifElseBranch.getThenBlock().accept(this);
        var newOp2 = new NopInst();
        addEdge(mLastControlInstruction, newOp2);
        if (ifElseBranch.getElseBlock() != null) {
            mLastControlInstruction = ji;
            ifElseBranch.getElseBlock().accept(this);
            addEdge(mLastControlInstruction, newOp2);
        } else {
            ji.setNext(0, newOp2);
        }
        addEdge(prevInstruction, ji);
        mLastControlInstruction = newOp2;
    }

    @Override
    public void visit(WhileLoop whileLoop) {
        Instruction checkInstruction = mLastControlInstruction;
        whileLoop.getCondition().accept(this);
        Instruction prevInstruction = mLastControlInstruction;
        var ji = new JumpInst((LocalVar) mExpressionValue);
        var newNop = new NopInst();
        var newNop2 = new NopInst();
        ji.setNext(0, newNop);
        ji.setNext(1, newNop2);
        mLastControlInstruction = newNop2;
        whileLoop.getBody().accept(this);
        addEdge(mLastControlInstruction, checkInstruction.getNext(0));
        addEdge(prevInstruction, ji);
        mLastControlInstruction = newNop;
    }
}
