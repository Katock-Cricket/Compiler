package Generate.middle.code;

import Generate.middle.operand.Symbol;

public class Input extends Node{
    public Symbol dst;
    private final String op = "GetInt";

    public Input(Symbol dst){
        this.dst=dst;
    }

    @Override
    public String toString() {
        return op + " " + dst;
    }
}
