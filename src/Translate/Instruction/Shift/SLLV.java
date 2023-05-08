package Translate.Instruction.Shift;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class SLLV extends DSS {
    public SLLV(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("sllv");
    }
}
