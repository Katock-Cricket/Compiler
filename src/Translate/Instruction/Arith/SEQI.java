package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SEQI extends DSI {
    public SEQI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("seq");
    }
}
