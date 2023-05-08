package Translate.Instruction.Shift;

import Translate.Hardware.Reg;
import Translate.Instruction.DSI;

public class SRA extends DSI {
    public SRA(Reg dst, Reg src, int imm){
        super(dst, src, imm);
    }

    @Override
    public String instrToString() {
        return toString("sra");
    }
}
