package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.ST;

public class BLTZ extends ST {
    public BLTZ(Reg src, String target){
        super(src, target);
    }

    @Override
    public String instrToString() {
        return toString("bltz");
    }
}
