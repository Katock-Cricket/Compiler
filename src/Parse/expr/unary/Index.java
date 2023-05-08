package Parse.expr.unary;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.expr.multi.Exp;

import java.io.PrintStream;
import java.util.Objects;

public class Index implements Printer {
    public Token LBk, RBk;//"[" and "]"
    public Exp index;//index of array, can be exp or number

    public Index(Token l, Token r, Exp exp){
        LBk=l;
        RBk=r;
        index=exp;
    }

    @Override
    public void output(PrintStream p){
        LBk.output(p);
        index.output(p);
        if (Objects.nonNull(RBk))
            RBk.output(p);
    }
}
