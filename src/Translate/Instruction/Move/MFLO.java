package Translate.Instruction.Move;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

public class MFLO extends Instruction {
    public final Reg dst;
    public MFLO(Reg dst){
        this.dst=dst;
    }

    @Override
    public String instrToString() {
        return "mflo $"+ dst.name;
    }
}
