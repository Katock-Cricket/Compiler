package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SLTI extends DSI {
    public SLTI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("slti");
    }
}
