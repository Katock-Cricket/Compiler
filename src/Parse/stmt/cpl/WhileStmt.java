package Parse.stmt.cpl;

import Tokenize.token.Token;
import Parse.expr.multi.Cond;

import java.io.PrintStream;
import java.util.Objects;

public class WhileStmt implements CplStmt{
    public final Token _while, LP, RP;
    public final Cond cond;
    public final Stmt stmt;

    public WhileStmt(Token _while, Token LP, Cond cond, Token RP, Stmt stmt){
        this._while=_while;
        this.LP=LP;
        this.cond=cond;
        this.RP=RP;
        this.stmt=stmt;
    }

    @Override
    public void output(PrintStream p){
        _while.output(p);
        LP.output(p);
        cond.output(p);
        if (Objects.nonNull(RP))
            RP.output(p);
        stmt.output(p);
    }
}
