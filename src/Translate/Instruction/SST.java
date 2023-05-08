package Translate.Instruction;

import Translate.Hardware.Reg;

public abstract class SST extends Instruction {
    public final Reg src1, src2;
    public String target;
    public SST(Reg src1, Reg src2, String target){
        this.src1=src1;
        this.src2=src2;
        this.target=target;
    }
    public String toString(String name){
        return String.format("%s $%s, $%s, %s", name, src1.name, src2.name, target);
    }
}
