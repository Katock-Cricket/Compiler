package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class LB extends ROB {
    public LB(Reg dst, int offset, Reg base){
        super(dst, offset, base);
    }

    @Override
    public String instrToString() {
        return toString("lb");
    }
}
