package Parse.stmt.spl;

import Tokenize.token.Token;
import Parse.expr.unary.LVal;

import java.io.PrintStream;
import java.util.Objects;

public class GetIntStmt implements SplStmt{
    public final LVal lVal;
    private final Token assign;
    private final Token getInt;
    public final Token LP;
    private final Token RP;

    public GetIntStmt(LVal lVal1, Token assign1, Token getInt1, Token LP1, Token RP1){
        lVal=lVal1;
        assign=assign1;
        getInt=getInt1;
        LP=LP1;
        RP=RP1;
    }

    @Override
    public int lineNumber(){
        return getInt.lineNumber;
    }

    @Override
    public void output(PrintStream p){
        lVal.output(p);
        assign.output(p);
        getInt.output(p);
        LP.output(p);
        if (Objects.nonNull(RP))
            RP.output(p);
    }
}
