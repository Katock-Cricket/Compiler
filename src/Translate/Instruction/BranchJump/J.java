package Translate.Instruction.BranchJump;


import Translate.Instruction.Instruction;

public class J extends Instruction {
    public String target;
    public J(String target){
        this.target=target;
    }

    @Override
    public String instrToString() {
        return "j "+target;
    }
}
