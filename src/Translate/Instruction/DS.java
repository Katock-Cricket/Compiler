package Translate.Instruction;

import Translate.Hardware.Reg;

// op dst, src
public abstract class DS extends Instruction{
    public final Reg src, dst;
    public DS(Reg dst, Reg src){
        this.src=src;
        this.dst=dst;
    }

    public String toString(String name){
        return String.format("%s $%s, $%s", name, dst.name, src.name);
    }
}
