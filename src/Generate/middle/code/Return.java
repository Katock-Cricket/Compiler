package Generate.middle.code;

import Generate.middle.operand.Operand;

public class Return extends Node{
    public Operand value;
    public final ReturnType type;
    private final String op = "Return";

    private Return(Operand operand, ReturnType funcType){
        value=operand;
        type=funcType;
    }

    public static Return IntReturn(Operand value){
        return new Return(value, ReturnType.INT);
    }

    public static Return VoidReturn(){
        return new Return(null, ReturnType.VOID);
    }

    @Override
    public String toString() {
        return op+(type.equals(ReturnType.VOID)?"":" "+value);
    }
}
