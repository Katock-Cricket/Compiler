package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DS;

public class CLO extends DS {
    public CLO(Reg dst, Reg src){
        super(dst, src);
    }

    @Override
    public String instrToString() {
        return toString("clo");
    }
}
