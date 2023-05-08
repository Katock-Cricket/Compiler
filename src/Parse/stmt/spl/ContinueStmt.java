package Parse.stmt.spl;

import Tokenize.token.Token;

import java.io.PrintStream;

public class ContinueStmt implements SplStmt{
    private final Token _continue;

    public ContinueStmt(Token token){
        _continue=token;
    }

    public Token get_continue() {
        return _continue;
    }

    @Override
    public int lineNumber(){
        return _continue.lineNumber;
    }

    @Override
    public void output(PrintStream p){
        _continue.output(p);
    }
}
