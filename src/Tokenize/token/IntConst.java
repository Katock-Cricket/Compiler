package Tokenize.token;

public class IntConst extends Token {
    public final int value;

    public IntConst(String literal, int line) {
        super(TokenType.INTCON, line, literal);
        this.value = Integer.parseInt(literal);
    }
}
