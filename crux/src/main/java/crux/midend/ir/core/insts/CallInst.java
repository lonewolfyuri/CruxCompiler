package crux.midend.ir.core.insts;

import crux.midend.ir.core.AddressVar;
import crux.midend.ir.core.Instruction;
import crux.midend.ir.core.Value;
import crux.midend.ir.core.LocalVar;

import java.util.*;
import java.util.function.Function;

/**
 * Calls a function with the provided arguments.
 * <p>
 * Operation (pseudo-code):
 * <pre>
 * {@code
 * for (var param in params)
 *     push(param)
 * call(callee)
 * }
 * </pre>
 */
public final class CallInst extends Instruction {
    static private List<Value> merge(AddressVar callee, List<LocalVar> params) {
        Value[] l = new Value[1+params.size()];
        l[0]=callee;
        for(int i=1; i<l.length;i++)
          l[i]=params.get(i-1);
        return List.of(l);
    }

    public CallInst(LocalVar destVar, AddressVar callee, List<LocalVar> params) {
      super(destVar, merge(callee, params));
    }

    public CallInst(AddressVar callee, List<LocalVar> params) {
      super(merge(callee, params));
    }

    public AddressVar getCallee() {
        return (AddressVar) mOperands.get(0);
    }

    public List<Value> getParams() {
      return mOperands.subList(1, mOperands.size());
    }

    public LocalVar getDst() {
        return (LocalVar) mDestVar;
    }
  
    @Override
    public void accept(InstVisitor v) {
        v.visit(this);
    }

    @Override
    public String format(Function<Value, String> valueFormatter) {
        var callee = valueFormatter.apply(getCallee());
        String paramstr = "";
        List<Value> lparams = getParams();
        for(Value p: lparams) {
          paramstr += valueFormatter.apply(p);
        }
        if(mDestVar != null) {
            var destVar = valueFormatter.apply(mDestVar);
            return String.format("%s = call %s (%s)", destVar, callee, paramstr);
        } else {
            return String.format("call %s (%s)", callee, paramstr);
        }
    }
}
