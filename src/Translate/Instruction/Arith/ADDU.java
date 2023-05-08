package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class ADDU extends DSS {
    public ADDU(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("addu");
    }
}
