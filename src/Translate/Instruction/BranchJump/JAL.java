package Translate.Instruction.BranchJump;

import Translate.Instruction.Instruction;

public class JAL extends Instruction {
    public String target;
    public JAL(String target){
        this.target=target;
    }

    @Override
    public String instrToString() {
        return "jal "+target;
    }
}
