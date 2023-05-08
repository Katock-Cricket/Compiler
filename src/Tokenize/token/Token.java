package Tokenize.token;


import java.io.PrintStream;

public abstract class Token {

    public final TokenType tokenType;
    public final int lineNumber;
    public final String content;

    public Token(TokenType tokenType, int lineNumber, String content) {
        this.tokenType = tokenType;
        this.lineNumber = lineNumber;
        this.content = content;
    }
    public void output(PrintStream ps) {
        ps.println(tokenType.name() + " " + content);
    }

    @Override
    public String toString() {
        return content;
    }

    public static Token newInstance(TokenType refTokenType, int line, String content) {
        switch (refTokenType) {
            case IDENFR: return new Ident(content, line);
            case INTCON: return new IntConst(content, line);
            case STRCON: return new FormatString(content, line);
            default: return new ReservedToken(refTokenType, line, content);
        }
    }
}
