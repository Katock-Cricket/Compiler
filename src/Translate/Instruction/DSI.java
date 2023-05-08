package Translate.Instruction;

import Translate.Hardware.Reg;

// op dst, src, imm
public abstract class DSI extends Instruction{
    public final Reg dst, src;
    private final int imm;
    public DSI(Reg dst, Reg src, int imm){
        this.dst=dst;
        this.src=src;
        this.imm=imm;
    }
    public String toString(String name){
        return String.format("%s $%s, $%s, %d", name, dst.name, src.name, imm);
    }
}
