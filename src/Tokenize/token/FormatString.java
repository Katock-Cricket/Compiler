package Tokenize.token;

public class FormatString extends Token {

    public final String inner;

    public FormatString(String str, int line) {
        super(TokenType.STRCON, line, str);
        assert str.length() >= 2 && str.charAt(0) == '\"' && str.charAt(str.length() - 1) == '\"';
        this.inner = str.substring(1, str.length() - 1);
    }
}
