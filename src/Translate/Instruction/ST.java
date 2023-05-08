package Translate.Instruction;

import Translate.Hardware.Reg;
public abstract class ST extends Instruction {
    public String target;
    public final Reg src;
    public ST(Reg src, String target){
        this.target=target;
        this.src=src;
    }
    public String toString(String name){
        return String.format("%s $%s, %s", name, src.name, target);
    }
}
