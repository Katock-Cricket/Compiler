package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SLEI extends DSI {
    public SLEI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("sle");
    }
}
