package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.ST;

public class BGTZ extends ST {
    public BGTZ(Reg src, String target){
        super(src, target);
    }

    @Override
    public String instrToString() {
        return toString("bgtz ");
    }
}
