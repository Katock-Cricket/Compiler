package Translate.Instruction.Shift;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class SRAV extends DSS {
    public SRAV(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("srav");
    }
}
