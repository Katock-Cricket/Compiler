package Parse.func;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class FuncParam implements Printer {
    private final Token bType;
    public final Ident ident;
    public FirstDim first;
    public final List<ArrayDim> dims;

    public FuncParam(Token bType, Ident ident, FirstDim first, List<ArrayDim> dims){
        this.bType=bType;
        this.ident=ident;
        this.first=first;
        this.dims=dims;
    }

    @Override
    public void output(PrintStream p) {
        bType.output(p);
        ident.output(p);
        if (Objects.nonNull(first))
            first.output(p);
        Iterator<ArrayDim>it= dims.iterator();
        while(it.hasNext()){
            it.next().output(p);
        }
        p.println("<FuncFParam>");
    }
}
