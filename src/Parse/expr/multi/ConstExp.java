package Parse.expr.multi;

import java.io.PrintStream;

public class ConstExp extends Exp {
    private final String name="<ConstExp>";
    public ConstExp(AddExp addExp1){
        super(addExp1);
    }

    @Override
    public void output(PrintStream p){
        this.addExp.output(p);
        p.println(name);
    }
}
