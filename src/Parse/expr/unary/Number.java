package Parse.expr.unary;

import Tokenize.token.IntConst;

import java.io.PrintStream;

public class Number implements BasePrimaryExp {
    public IntConst number;
    private final String name = "<Number>";

    public Number(IntConst intConst){
        number=intConst;
    }

    @Override
    public void output(PrintStream p){
        number.output(p);
        p.println(name);
    }
}
