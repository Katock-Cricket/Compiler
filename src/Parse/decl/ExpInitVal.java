package Parse.decl;

import Parse.expr.multi.Exp;

import java.io.PrintStream;

public class ExpInitVal implements InitVal{
    public final boolean isConst;
    public final Exp exp;

    public ExpInitVal(boolean isConst, Exp exp){
        this.isConst = isConst;
        this.exp=exp;
    }

    @Override
    public void output(PrintStream p){
        exp.output(p);
        if (isConst)
            p.println("<ConstInitVal>");
        else
            p.println("<InitVal>");
    }
}
