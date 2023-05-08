package Parse;

import Tokenize.token.Token;
import Tokenize.token.TokenType;

import java.util.Iterator;
import java.util.ListIterator;

public class ParserUtil {
    public static void handleEof(String name, Iterator<Token> iterator, int maxLineNum){
        if(!iterator.hasNext())
            throw new Error("Unexpected EOF: "+maxLineNum+" "+name);
    }

    public static Token getTokenSpecial(TokenType tokenType, String name, Iterator<Token> it, int maxLine){
        handleEof(name, it, maxLine);
        Token token = it.next();
        if(tokenType.equals(token.tokenType))
            return token;
        throw new Error("WrongToken: " + it.next().lineNumber+" "+name+" "+it.next()+" "+ tokenType);
    }

    public static Token getTokenIgnoreNull(TokenType tokenType, String name, ListIterator<Token> it, int maxLine) {
        handleEof(name, it, maxLine);
        Token token = it.next();
        if(tokenType.equals(token.tokenType))
            return token;
        it.previous();
        return null;
    }

    public static Token getTokenNext(String name, Iterator<Token> iterator, int maxNum) {
        handleEof(name, iterator, maxNum);
        return iterator.next();
    }
}
