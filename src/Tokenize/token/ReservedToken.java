package Tokenize.token;

public class ReservedToken extends Token {
    public ReservedToken(TokenType tokenType, int line, String content) {
        super(tokenType, line, content);
    }
}
