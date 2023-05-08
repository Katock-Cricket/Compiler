package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.SST;

public class BEQ extends SST {
    public BEQ(Reg src1, Reg src2, String target){
        super(src1, src2, target);
    }

    @Override
    public String instrToString() {
        return toString("beq");
    }
}
