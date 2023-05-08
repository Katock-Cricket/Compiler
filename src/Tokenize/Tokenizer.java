package Tokenize;

import Tokenize.token.Token;
import Tokenize.token.TokenType;

import java.io.PrintStream;
import java.util.*;

public class Tokenizer {
    private final List<Token> tokens = new LinkedList<>();
    private int maxLineNumber = 0;

    public Tokenizer(Source source){
        while (!source.reachedEndOfFile()) {// for evey word in source
            if(source.skipBlanksComments()) continue;
            boolean matchToken = false;
            for (TokenType refTokenType : TokenType.values()) {
                String token = source.matchFollowing(refTokenType.getPattern()); // find the match tokenType
                if (Objects.nonNull(token)) {
                    this.append(Token.newInstance(refTokenType, source.getLineIndex(), token)); // put into list(type, line number, string)
                    source.forward(token.length()); // get next word
                    matchToken = true;
                    break;
                }
            }
            if (!source.reachedEndOfFile() && !matchToken) {
                throw new Error("UndefinedToken: "+source.getLineIndex()+" "+source.getCurrentLine());
            }
        }
    }

    public void append(Token token) {
        tokens.add(token);
        maxLineNumber = Math.max(maxLineNumber, token.lineNumber);
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public int getMaxLineNumber() {
        return maxLineNumber;
    }

    public void output(PrintStream out) {
        tokens.forEach(token -> token.output(out));
    }
}
