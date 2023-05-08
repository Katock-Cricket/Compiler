package Translate.Instruction.Logic;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class ANDI extends DSI {
    public ANDI(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("andi");
    }
}
