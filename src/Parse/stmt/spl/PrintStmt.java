package Parse.stmt.spl;

import Tokenize.token.FormatString;
import Tokenize.token.Token;
import Parse.expr.multi.Exp;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class PrintStmt implements SplStmt{
    public final Token printf, LP, RP;
    public final FormatString formatString;
    private final List<Token> commas;
    public final List<Exp>params;

    public PrintStmt(Token printf, Token LP, FormatString formatString, List<Token> commas, List<Exp> params, Token RP){
        this.commas=commas;
        this.formatString=formatString;
        this.LP=LP;
        this.params=params;
        this.printf=printf;
        this.RP=RP;
    }

    @Override
    public int lineNumber(){
        return printf.lineNumber;
    }

    @Override
    public void output(PrintStream p){
        printf.output(p);
        LP.output(p);
        formatString.output(p);
        Iterator<Token> commasIt = commas.iterator();
        Iterator<Exp> paramsIt = params.iterator();
        while(commasIt.hasNext() && paramsIt.hasNext()){
            commasIt.next().output(p);
            paramsIt.next().output(p);
        }
        if (Objects.nonNull(RP))
            RP.output(p);
    }
}
