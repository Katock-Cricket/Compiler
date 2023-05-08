package Generate.middle.code;

import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;

public class StoreInstr extends Node {
    private final String op = "Store";
    public final Symbol addr;
    public Operand src;

    public StoreInstr(Operand src, Symbol addr){
        this.addr = addr;
        this.src = src;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", op, src, addr);
    }
}
