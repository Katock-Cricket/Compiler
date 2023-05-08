package Parse.expr.multi;

import Parse.Printer;

import java.io.PrintStream;

public class Exp implements Printer {
    public final AddExp addExp;
    private final String name = "<Exp>";

    public Exp(AddExp addExp1){
        addExp=addExp1;
    }

    @Override
    public void output(PrintStream p){
        addExp.output(p);
        p.println(name);
    }
}
