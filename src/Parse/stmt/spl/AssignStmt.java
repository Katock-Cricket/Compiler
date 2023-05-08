package Parse.stmt.spl;

import Tokenize.token.Token;
import Parse.expr.multi.Exp;
import Parse.expr.unary.LVal;

import java.io.PrintStream;

public class AssignStmt implements SplStmt{
    public final LVal lVal;
    private final Token assign;
    public final Exp exp;

    public AssignStmt(LVal lVal1, Token token, Exp exp1){
        lVal=lVal1;
        assign=token;
        exp=exp1;
    }

    @Override
    public int lineNumber() {
        return lVal.ident.lineNumber;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public void output(PrintStream p){
        lVal.output(p);
        assign.output(p);
        exp.output(p);
    }
}
