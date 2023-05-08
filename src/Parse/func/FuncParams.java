package Parse.func;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class FuncParams implements Printer {
    public final FuncParam first;
    private final List<Token> commas;
    public final List<FuncParam> params;

    public FuncParams(FuncParam first, List<Token> commas, List<FuncParam> params){
        this.first=first;
        this.commas=commas;
        this.params=params;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        Iterator<Token> commasIt = commas.iterator();
        Iterator<FuncParam> paramsIt = params.iterator();
        while (commasIt.hasNext()){
            commasIt.next().output(p);
            paramsIt.next().output(p);
        }
        p.println("<FuncFParams>");
    }
}
