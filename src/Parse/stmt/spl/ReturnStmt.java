package Parse.stmt.spl;

import Tokenize.token.Token;
import Parse.expr.multi.Exp;

import java.io.PrintStream;
import java.util.Objects;

public class ReturnStmt implements SplStmt{
    public final Token _return;
    public final Exp exp;

    public ReturnStmt(Token _return1, Exp exp1){
        _return=_return1;
        exp=exp1;
    }

    @Override
    public int lineNumber(){
        return _return.lineNumber;
    }

    @Override
    public void output(PrintStream p){
        _return.output(p);
        if (Objects.nonNull(exp))
            exp.output(p);
    }
}
