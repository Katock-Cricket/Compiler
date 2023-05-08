package Parse.expr.unary;

import Tokenize.token.Ident;
import Tokenize.token.Token;

import java.io.PrintStream;

public class FuncCall implements BaseUnaryExp {
    public Ident name;
    public Token LP, RP;
    public FuncRParams funcRParams;

    public FuncCall(Ident ident, Token l, Token r, FuncRParams params){
        name=ident;
        LP=l;
        RP=r;
        funcRParams=params;
    }

    @Override
    public void output(PrintStream p){
        this.name.output(p);
        this.LP.output(p);
        if (this.funcRParams!=null)
            this.funcRParams.output(p);
        if (this.RP!=null)
            this.RP.output(p);
    }
}
