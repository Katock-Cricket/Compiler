package Generate.middle.code;

import Generate.middle.operand.Symbol;
import Generate.middle.operand.Operand;

public class BinaryInstr extends Node{
    public final BinaryOp op;
    public Operand src1;
    public Operand src2;
    public Symbol dst;

    public BinaryInstr(BinaryOp op, Operand src1, Operand src2, Symbol dst){
        this.op=op;
        this.src1=src1;
        this.src2=src2;
        this.dst=dst;
    }

    @Override
    public String toString() {
        return op.name() + " " + src1 + ", " + src2 + ", " + dst;
    }
}
