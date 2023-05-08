package Parse.expr.unary;

import Tokenize.token.Token;
import java.util.List;

public class UnaryOp{
    public List<Token> ops;
    public UnaryOp(List<Token> tokenList){
        ops = tokenList;
    }
}
