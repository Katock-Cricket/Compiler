package Translate.Instruction.Move;

import Translate.Hardware.Reg;
import Translate.Instruction.Instruction;

public class MFHI extends Instruction {
    public final Reg dst;
    public MFHI(Reg dst){
        this.dst=dst;
    }

    @Override
    public String instrToString() {
        return "mfhi $"+ dst.name;
    }
}
