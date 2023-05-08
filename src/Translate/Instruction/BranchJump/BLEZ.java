package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.ST;

public class BLEZ extends ST {
    public BLEZ(Reg src, String target){
        super(src, target);
    }

    @Override
    public String instrToString() {
        return toString("blez");
    }
}
