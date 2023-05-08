package Translate.Instruction.Logic;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

public class LI extends Instruction {
    private final Reg dst;
    private final int imm;
    public LI(Reg dst, int imm){
        this.dst=dst;
        this.imm=imm;
    }

    @Override
    public String instrToString() {
        return String.format("li $%s, %d", dst.name,imm);
    }
}
