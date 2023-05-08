package Parse.func;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.expr.multi.ConstExp;

import java.io.PrintStream;
import java.util.Objects;

public class ArrayDim extends FirstDim implements Printer {
    public final ConstExp length;

    public ArrayDim(Token l,  ConstExp len, Token r){
        super(l,r);
        length=len;
    }

    @Override
    public void output(PrintStream p) {
        LBk.output(p);
        length.output(p);
        if (Objects.nonNull(RBk))
            RBk.output(p);
    }
}
