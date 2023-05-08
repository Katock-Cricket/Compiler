package Generate.middle.code;

import Generate.middle.operand.Operand;

public class PrintInt extends Node{
    public Operand value;

    public PrintInt(Operand operand){
        value=operand;
    }

    @Override
    public String toString() {
        return "PRINT_INT "+value;
    }
}
