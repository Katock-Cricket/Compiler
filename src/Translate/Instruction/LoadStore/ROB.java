package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

// op reg, offset(base)
public abstract class ROB extends Instruction {
    private final Reg reg , base;
    private final int offset;
    public ROB(Reg reg, int offset, Reg base){
        this.reg = reg;
        this.offset=offset;
        this.base=base;
    }
    public String toString(String name) {
        return String.format("%s $%s, %d($%s)",name, reg.name, offset, base.name);
    }
}
