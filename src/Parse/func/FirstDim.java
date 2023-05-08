package Parse.func;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.Objects;

public class FirstDim implements Printer {
    public Token LBk, RBk;

    public FirstDim(Token LBk, Token RBk){
        this.LBk=LBk;
        this.RBk=RBk;
    }

    @Override
    public void output(PrintStream p) {
        LBk.output(p);
        if (Objects.nonNull(RBk))
            RBk.output(p);
    }
}
