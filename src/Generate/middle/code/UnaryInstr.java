package Generate.middle.code;

import Generate.middle.operand.Symbol;
import Generate.middle.operand.Operand;

public class UnaryInstr extends Node{
    public final UnaryOp op;
    public Operand src;
    public Symbol dst;

    public UnaryInstr(UnaryOp op, Operand src, Symbol dst){
        this.op = op;
        this.src=src;
        this.dst=dst;
    }

    @Override
    public String toString() {
        return op.name() + " " + src + ", " + dst;
    }
}
