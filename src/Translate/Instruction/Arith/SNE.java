package Translate.Instruction.Arith;

import Translate.Hardware.Reg;
import Translate.Instruction.DSS;

public class SNE extends DSS {
    public SNE(Reg dst, Reg src1, Reg src2){
        super(dst, src1, src2);
    }

    @Override
    public String instrToString() {
        return toString("sne");
    }
}
