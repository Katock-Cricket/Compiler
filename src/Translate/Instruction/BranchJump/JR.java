package Translate.Instruction.BranchJump;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

public class JR extends Instruction {
    public final Reg src;
    public JR(Reg src){
        this.src=src;
    }

    @Override
    public String instrToString() {
        return String.format("jr $%s", src.name);
    }
}
