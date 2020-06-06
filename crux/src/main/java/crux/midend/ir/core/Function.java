package crux.midend.ir.core;

import crux.midend.ir.Formattable;
import crux.frontend.types.FuncType;
import crux.frontend.types.Type;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A function which is the lowered version of a {@link crux.frontend.ast.FunctionDefinition}. The
 * difference to the AST version is, that the body of the function does not consist of a list of statements, but instead
 * it is a graph, in which instructions are nodes and the control flow are the edges.
 */
@SuppressWarnings("UnstableApiUsage")
public final class Function implements Formattable {
    private String mFuncName;
    private List<LocalVar> mArgs;
    private FuncType mFuncType;

    private static final int FUNC_FORMAT_INDENT = 2;
    private int mTempVarCounter, mTempAddressVarCounter;
    private Instruction startInstruction;

    public Function(String name, List<LocalVar> args, FuncType funcType) {
        mFuncName = name;
        mArgs = List.copyOf(args);
        mFuncType = funcType;
        mTempVarCounter = 0;
        mTempAddressVarCounter = 0;
    }

    public List<LocalVar> getArguments() {
        return List.copyOf(mArgs);
    }
  
    public String getName() { return mFuncName; }

    public FuncType getFuncType() { return mFuncType; }

    public LocalVar getTempVar(Type type) {
        var name = String.format("t%d", mTempVarCounter++);
        return new LocalVar(type, name);
    }
    public AddressVar getTempAddressVar(Type type) {
        var name = String.format("t%d", mTempAddressVarCounter++);
        return new AddressVar(type, name);
    }

    public Instruction getStart() {
        return startInstruction;
    }
  
    public void setStart(Instruction inst) {
        startInstruction = inst;
    }

    @Override
    public String format(java.util.function.Function<Value, String> valueFormatter) {
        var funcName = getName();
        var funcDotBuilder = new StringBuilder();
        var indent = FUNC_FORMAT_INDENT;
        funcDotBuilder.append(" ".repeat(indent))
                .append("subgraph cluster_").append(funcName).append(" {\n");
        indent *= 2;

        // Styles
        funcDotBuilder.append(" ".repeat(indent)).append("style=filled;")
                .append("color=lightgrey;")
                .append("node [style=filled, color=white];")
                .append("\n");

        final var funcType = "function %%%s(%s) -> %s";
        var argStrStream = mArgs.stream().map(valueFormatter).collect(Collectors.toList());
        var argStr = String.join(",", argStrStream);
        var funcHeader = String.format(funcType, getName(), argStr, getFuncType().getRet());
        funcDotBuilder.append(" ".repeat(indent))
                .append(String.format("label=\"%s\";\n", funcHeader));

        // Print nodes
        int nodeCounter = 0;
        final var nodePrefix = funcName + "_n";
        Map<Instruction, String> nodeIdMap = new HashMap<>();
        // Only print edge labels for nodes that have multiple (out) edges
        Instruction start = getStart();
        Stack<Instruction> tovisit = new Stack<>();

        tovisit.add(start);
        nodeIdMap.put(start, nodePrefix+(nodeCounter++));

        while(!tovisit.isEmpty()) {
            Instruction inst = tovisit.pop();
            String srcId = nodeIdMap.get(inst);
            
            funcDotBuilder.append(" ".repeat(indent))
                            .append(srcId)
                            .append(" [label=\"");
            funcDotBuilder.append(inst.format(valueFormatter))
                            .append("\"];\n");

            for(int i = 0; i < inst.numNext(); i++) {
                Instruction dst = inst.getNext(i);
                if (!nodeIdMap.containsKey(dst)) {
                    nodeIdMap.put(dst, nodePrefix+(nodeCounter++));
                    tovisit.push(dst);
                }
                String dstId = nodeIdMap.get(dst);
                funcDotBuilder.append(" ".repeat(indent))
                    .append(srcId)
                    .append(" -> ")
                    .append(dstId);                
                if (inst.numNext() == 2) {
                    funcDotBuilder.append(" [label=\"  ");
                    if (i == 0)
                        funcDotBuilder.append("False");
                    else
                        funcDotBuilder.append("True");
                    funcDotBuilder.append("  \"]");
                }
                funcDotBuilder.append(";\n");
            }
        }

        // End
        indent /= 2;
        funcDotBuilder.append(" ".repeat(indent))
                .append("}\n");

        return funcDotBuilder.toString();
    }
}
