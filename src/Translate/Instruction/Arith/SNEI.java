package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SNEI extends DSI {
    public SNEI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("sne");
    }
}
