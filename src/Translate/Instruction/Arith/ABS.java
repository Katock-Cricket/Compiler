package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DS;

public class ABS extends DS {
    public ABS(Reg dst, Reg src){
        super(dst, src);
    }

    @Override
    public String instrToString() {
        return toString("abs");
    }
}
