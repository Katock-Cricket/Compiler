package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.SST;

public class BNE extends SST {
    public BNE(Reg src1, Reg src2, String target){
        super(src1, src2, target);
    }

    @Override
    public String instrToString() {
        return toString("bne");
    }
}
