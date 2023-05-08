package Generate.middle.code;
import Generate.middle.operand.Symbol;

public class LoadInstr extends Node{
    private final String op = "Load";
    public final Symbol addr;
    public Symbol dst;

    public LoadInstr(Symbol dst, Symbol addr){
        this.addr = addr;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", op, dst, addr);
    }
}
