package Translate.Instruction;

import Translate.Hardware.Reg;

// op dst, src1, src2
public abstract class DSS extends Instruction{
    public final Reg dst, src1, src2;
     public DSS(Reg dst, Reg src1, Reg src2){
         this.dst=dst;
         this.src1=src1;
         this.src2=src2;
     }

     public String toString(String name){
         return String.format("%s $%s, $%s, $%s", name, dst.name, src1.name, src2.name);
     }
}
