package crux.midend.ir.core;

import crux.midend.ir.Formattable;

import java.util.List;
import java.util.Vector;
import crux.midend.ir.core.insts.InstVisitor;

/**
 * The base class for all instructions. Every instruction consists of a destination variable and a list of operands.
 * Note that not every instruction needs a destination variable (for example a jump instruction that takes a target
 * address as operand). Further, the list operands can be empty as well (e.g. a nop instruction that does nothing.)
 */
public abstract class Instruction implements Formattable {
    protected Variable mDestVar;
    protected List<Value> mOperands;
    protected Vector<Instruction> next;
  
    protected Instruction(Variable destVar, List<Value> operands) {
        mDestVar = destVar;
        mOperands = List.copyOf(operands);
        next = new Vector<>();
    }

    protected Instruction(List<Value> operands) {
        mDestVar = null;
        mOperands = List.copyOf(operands);
        next = new Vector<>();
    }
  
    public abstract void accept(InstVisitor v);

    public Instruction getNext(int i) {
        if (i >= numNext())
            return null;
        else
            return next.get(i);
    }

    public int numNext() {
        return next.size();
    }
  
    public void setNext(int i, Instruction inst) {
      if (next.size() <= i)
        next.setSize(i + 1);
      next.set(i, inst);
    }
}
