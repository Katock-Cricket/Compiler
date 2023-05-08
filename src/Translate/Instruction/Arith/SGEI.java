package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SGEI extends DSI {
    public SGEI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("sge");
    }
}
