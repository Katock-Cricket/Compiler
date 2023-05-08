package Tokenize.token;

import java.util.regex.Pattern;

public enum TokenType {
    MAINTK("main", true),
    CONSTTK("const", true),
    INTTK("int", true),
    BREAKTK("break", true),
    CONTINUETK("continue", true),
    IFTK("if", true),
    ELSETK("else", true),
    VOIDTK("void", true),
    WHILETK("while", true),
    GETINTTK("getint", true),
    PRINTFTK("printf", true),
    RETURNTK("return", true),

    IDENFR("[_A-Za-z][_A-Za-z0-9]*"),
    INTCON("[0-9]+"),
    STRCON("\\\"[^\\\"]*\\\""),

    AND("&&"),
    OR("\\|\\|"),
    LEQ("<="),
    GEQ(">="),
    EQL("=="),
    NEQ("!="),

    PLUS("\\+"),
    MINU("-"),
    MULT("\\*"),
    DIV("/"),
    MOD("%"),
    LSS("<"),
    GRE(">"),
    NOT("!"),
    ASSIGN("="),
    SEMICN(";"),
    COMMA(","),
    LPARENT("\\("),
    RPARENT("\\)"),
    LBRACK("\\["),
    RBRACK("]"),
    LBRACE("\\{"),
    RBRACE("}");
    private final Pattern pattern;

    TokenType(String pattern) {
        this.pattern = Pattern.compile("^" + pattern);
    }

    TokenType(String pattern, boolean postAssert) {
        if (postAssert) {
            this.pattern = Pattern.compile("^" + pattern + "(?![_A-Za-z0-9])");
        } else {
            this.pattern = Pattern.compile("^" + pattern);
        }
    }

    public Pattern getPattern() {
        return pattern;
    }
}
