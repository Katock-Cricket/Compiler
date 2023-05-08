package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class LW extends ROB {
    public LW(Reg dst, int offset, Reg base){
        super(dst, offset, base);
    }

    @Override
    public String instrToString() {
        return toString("lw");
    }
}
