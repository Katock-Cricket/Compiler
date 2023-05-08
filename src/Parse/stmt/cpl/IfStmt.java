package Parse.stmt.cpl;

import Tokenize.token.Token;
import Parse.expr.multi.Cond;

import java.io.PrintStream;
import java.util.Objects;

public class IfStmt implements CplStmt{
    public final Token _if, _else;
    public final Token LP, RP;
    public final Cond cond;
    public final Stmt stmt1, stmt2;

    public IfStmt(Token _if, Token LP, Cond cond, Token RP, Stmt stmt1, Token _else, Stmt stmt2){
        this._if=_if;
        this._else=_else;
        this.LP=LP;
        this.RP=RP;
        this.cond=cond;
        this.stmt1=stmt1;
        this.stmt2=stmt2;
    }

    @Override
    public void output(PrintStream p){
        _if.output(p);
        LP.output(p);
        cond.output(p);
        if (Objects.nonNull(RP))
            RP.output(p);
        stmt1.output(p);
        if (Objects.nonNull(_else)){
            _else.output(p);
            stmt2.output(p);
        }

    }
}
