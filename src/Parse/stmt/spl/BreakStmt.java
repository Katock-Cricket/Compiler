package Parse.stmt.spl;

import Tokenize.token.Token;

import java.io.PrintStream;

public class BreakStmt implements SplStmt{
    public final Token _break;
    public BreakStmt(Token _break1){
        _break=_break1;
    }

    @Override
    public int lineNumber(){
        return _break.lineNumber;
    }

    @Override
    public void output(PrintStream p){
        _break.output(p);
    }
}
