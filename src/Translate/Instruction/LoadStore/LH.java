package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class LH extends ROB {
    public LH(Reg dst, int offset, Reg base){
        super(dst, offset, base);
    }

    @Override
    public String instrToString() {
        return toString("lh");
    }
}
