package Parse.expr.multi;

import Parse.Printer;

import java.io.PrintStream;

public class Cond implements Printer {
    private final String name="<Cond>";
    public final LOrExp lOrExp;

    public Cond(LOrExp lOrExp1){
        lOrExp=lOrExp1;
    }

    @Override
    public void output(PrintStream p){
        lOrExp.output(p);
        p.println(name);
    }
}
