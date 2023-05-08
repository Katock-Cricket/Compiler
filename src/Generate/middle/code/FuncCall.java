package Generate.middle.code;

import Generate.middle.operand.Symbol;
import Generate.middle.operand.Operand;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FuncCall extends Node{
    public final FuncScope func;
    public List<Operand> params;
    public final Symbol ret;
    public final ReturnType type;
    private final String op = "CALL";

    private FuncCall(FuncScope func, List<Operand> params, Symbol ret){
        this.func=func;
        this.params= Collections.unmodifiableList(params);
        this.ret=ret;
        if (Objects.nonNull(ret))
            this.type=ReturnType.INT;
        else
            this.type=ReturnType.VOID;
    }

    public static FuncCall intFuncCall(FuncScope func, List<Operand> params, Symbol ret){
        return new FuncCall(func,params,ret);
    }

    public static FuncCall voidFuncCall(FuncScope func, List<Operand> params){
        return new FuncCall(func,params,null);
    }

    @Override
    public String toString() {
        return op+" "+
                func.label+
                ", ["+params.stream()
                .map(Object::toString)
                .reduce((s,s2)->s+", "+s2).orElse("") +
                "]"+
                (type.equals(ReturnType.INT)?" -> "+ret:"");
    }
}
