package Generate.middle.code;

import Generate.middle.operand.Symbol;
import Generate.middle.operand.Operand;
//target = offset + base(arr or ptr)
public class Offset extends Node {
    public Symbol base;
    public Symbol target;
    public final String op = "Offset";
    public final Operand offset;

    public Offset(Symbol base, Operand offset, Symbol target){
        this.base = base;
        this.offset=offset;
        this.target=target;
    }

    @Override
    public String toString() {
        return op + " " + target + ", " + offset + ", " + base;
    }
}
