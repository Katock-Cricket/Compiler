package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.ST;

public class BGEZ extends ST {
    public BGEZ(Reg src, String target){
        super(src, target);
    }

    @Override
    public String instrToString() {
        return toString("bgez");
    }
}
