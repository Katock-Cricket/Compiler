package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DS;

public class CLZ extends DS {
    public CLZ(Reg dst, Reg src){
        super(dst, src);
    }

    @Override
    public String instrToString() {
        return toString("clz");
    }
}
