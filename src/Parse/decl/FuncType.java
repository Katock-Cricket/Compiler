package Parse.decl;

import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.Printer;

import java.io.PrintStream;

public class FuncType implements Printer {
    public final Token type;

    public FuncType(Token type) {
        assert type.tokenType.equals(TokenType.INTTK) || type.tokenType.equals(TokenType.VOIDTK);
        this.type = type;
    }

    @Override
    public void output(PrintStream ps) {
        type.output(ps);
        ps.println("<FuncType>");
    }
}
