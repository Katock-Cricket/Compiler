package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

public class MULT extends Instruction {
    public final Reg src1, src2;

    public MULT(Reg src1, Reg src2){
        this.src1=src1;
        this.src2=src2;
    }

    @Override
    public String instrToString() {
        return String.format("mult $%s, $%s", src1.name, src2.name);
    }
}
