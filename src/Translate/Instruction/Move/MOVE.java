package Translate.Instruction.Move;

import Translate.Hardware.Reg;
import Translate.Instruction.DS;

public class MOVE extends DS {
    public MOVE(Reg dst, Reg src){
        super(dst, src);
    }

    @Override
    public String instrToString() {
        return toString("move");
    }
}
