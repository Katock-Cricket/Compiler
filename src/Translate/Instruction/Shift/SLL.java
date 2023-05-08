package Translate.Instruction.Shift;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SLL extends DSI {
    public SLL(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("sll");
    }
}
