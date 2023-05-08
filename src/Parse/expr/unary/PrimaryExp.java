package Parse.expr.unary;


import java.io.PrintStream;

public class PrimaryExp implements BaseUnaryExp {
    public final BasePrimaryExp basePrimaryExp;
    private final String name = "<PrimaryExp>";

    public PrimaryExp(BasePrimaryExp base){
        basePrimaryExp=base;
    }

    public BasePrimaryExp getBasePrimaryExp(){
        return basePrimaryExp;
    }

    @Override
    public void output(PrintStream p){
        basePrimaryExp.output(p);
        p.println(name);
    }

}
