package crux.frontend;

import crux.frontend.ast.Position;
import crux.frontend.types.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SymbolTable {
    private final PrintStream err;
    private final List<Map<String, Symbol>> symbolScopes = new ArrayList<>();

    private boolean encounteredError = false;

    SymbolTable(PrintStream err) {
        this.err = err;
        enter();
        loadBuiltIns();
    }

    void loadBuiltIns() {
        Map<String, Symbol> currentScope = symbolScopes.get(symbolScopes.size() - 1);
        currentScope.put("readInt", new Symbol("readInt", new FuncType(new TypeList(), new IntType())));
        ArrayList<Type> printBoolTypes = new ArrayList<Type>();
        printBoolTypes.add(new BoolType());
        currentScope.put("printBool", new Symbol("printBool", new FuncType(new TypeList(printBoolTypes), new VoidType())));
        ArrayList<Type> printIntTypes = new ArrayList<Type>();
        printIntTypes.add(new IntType());
        currentScope.put("printInt", new Symbol("printInt", new FuncType(new TypeList(printIntTypes), new VoidType())));
        currentScope.put("println", new Symbol("println", new FuncType(new TypeList(), new VoidType())));
    }

    boolean hasEncounteredError() {
        return encounteredError;
    }

    void enter() {
        symbolScopes.add(new HashMap<String, Symbol>());
    }

    void exit() {
        symbolScopes.remove(symbolScopes.size() - 1);
    }

    Symbol add(Position pos, String name) {
        Map<String, Symbol> currentScope = symbolScopes.get(symbolScopes.size() - 1);
        if (currentScope.containsKey(name)) {
            err.printf("DeclareSymbolError%s[%s already exists.]%n", pos, name);
            encounteredError = true;
            return new Symbol(name, "DeclareSymbolError");
        }
        Symbol newSymbol = new Symbol(name);
        currentScope.put(name, newSymbol);
        return newSymbol;
    }

    Symbol add(Position pos, String name, Type type) {
        Map<String, Symbol> currentScope = symbolScopes.get(symbolScopes.size() - 1);
        if (currentScope.containsKey(name)) {
            err.printf("DeclareSymbolError%s[%s already exists.]%n", pos, name);
            encounteredError = true;
            return new Symbol(name, "DeclareSymbolError");
        }
        Symbol newSymbol = new Symbol(name, type);
        currentScope.put(name, newSymbol);
        return newSymbol;
    }

    Symbol lookup(Position pos, String name) {
        var symbol = find(name);
        if (symbol == null) {
            err.printf("ResolveSymbolError%s[Could not find %s.]%n", pos, name);
            encounteredError = true;
            return new Symbol(name, "ResolveSymbolError");
        } else {
            return symbol;
        }
    }

    private Symbol find(String name) {
        for (int i = symbolScopes.size() - 1; i >= 0; i--) {
            Map<String, Symbol> currentScope = symbolScopes.get(i);
            if (currentScope.containsKey(name)) {
                return currentScope.get(name);
            }
        }
        return null;
    }
}
