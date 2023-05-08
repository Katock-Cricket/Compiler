package Parse.expr.unary;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.expr.multi.Exp;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class FuncRParams implements Printer {
    public final Exp first;
    List<Token> commas;
    public List<Exp> params;
    private final String name = "<FuncRParams>";

    public FuncRParams(Exp f, List<Token> list, List<Exp> expList){
        first=f;
        commas=list;
        params=expList;
    }

    @Override
    public void output(PrintStream p){
        first.output(p);
        Iterator<Token> commasIt = this.commas.iterator();
        Iterator<Exp> paramsIt = this.params.iterator();
        while(commasIt.hasNext() && paramsIt.hasNext()){
            commasIt.next().output(p);
            paramsIt.next().output(p);
        }
        p.println(name);
    }
}
