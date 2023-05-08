package Translate.Instruction.LoadStore;

import Translate.Hardware.Reg;

public class SW extends ROB{
    public SW(Reg src, int offset, Reg base){
        super(src,offset,base);
    }

    @Override
    public String instrToString() {
        return toString("sw");
    }
}
