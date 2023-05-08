package Tokenize.token;

public class Ident extends Token {

    public final String name;

    public Ident(String name, int line) {
        super(TokenType.IDENFR, line, name);
        this.name = name;
    }
}
