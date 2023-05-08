package Parse.decl;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.expr.multi.ConstExp;

import java.io.PrintStream;
import java.util.Objects;

public class ArrayDef implements Printer {
    public final Token LBk, RBk;
    public final ConstExp constExp;

    public ArrayDef(Token LBk, ConstExp constExp, Token RBk){
        this.LBk=LBk;
        this.constExp=constExp;
        this.RBk=RBk;
    }

    @Override
    public void output(PrintStream p){
        LBk.output(p);
        constExp.output(p);
        if (Objects.nonNull(RBk))
            RBk.output(p);
    }
}
