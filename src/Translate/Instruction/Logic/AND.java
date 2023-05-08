package Translate.Instruction.Logic;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class AND extends DSS {
    public AND(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("and");
    }
}
