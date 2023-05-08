package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class SH extends ROB{
    public SH(Reg src, int offset, Reg base){
        super(src, offset, base);
    }

    @Override
    public String instrToString() {
        return toString("sh");
    }
}
