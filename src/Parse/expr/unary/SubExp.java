package Parse.expr.unary;

import Tokenize.token.Token;
import Parse.expr.multi.Exp;

import java.io.PrintStream;

public class SubExp implements BasePrimaryExp {
    public final Token LP, RP;
    public final Exp exp;

    public SubExp(Token l, Token r, Exp e){
        LP=l;
        RP=r;
        exp=e;
    }

    @Override
    public void output(PrintStream p){
        LP.output(p);
        exp.output(p);
        RP.output(p);
    }
}
