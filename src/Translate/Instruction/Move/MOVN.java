package Translate.Instruction.Move;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class MOVN extends DSS {
    public MOVN(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("movn");
    }
}
