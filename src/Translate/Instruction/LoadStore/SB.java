package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class SB extends ROB {
    public SB(Reg src, int offset, Reg base){
        super(src, offset, base);
    }

    @Override
    public String instrToString() {
        return toString("sb");
    }
}
