package Parse.func;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.decl.FuncType;
import Parse.expr.ExpParser;
import Parse.stmt.StmtParser;
import Parse.stmt.cpl.Block;

import java.util.*;

import static Parse.ParserUtil.*;

public class FuncParser {
    private final ListIterator<Token> iterator;
    private final int maxNum;

    public FuncParser(ListIterator<Token> listIterator, int num) {
        iterator = listIterator;
        maxNum = num;
    }

    public ArrayDim parseArrayDim(Token LBk)  {
        return new ArrayDim(LBk, new ExpParser(iterator, maxNum).parseConstExp(), getTokenIgnoreNull(TokenType.RBRACK, "<FuncFParams>", iterator, maxNum));
    }

    public FuncParam parseFuncParam()  {
        Token bType = getTokenSpecial(TokenType.INTTK, "<FuncFParam>", iterator, maxNum);
        Ident ident = (Ident) getTokenSpecial(TokenType.IDENFR, "<FuncFParams>", iterator, maxNum);
        if (!iterator.hasNext())
            return new FuncParam(bType, ident, null, Collections.emptyList());
        Token LBk = iterator.next();
        if (!LBk.tokenType.equals(TokenType.LBRACK)) {
            iterator.previous();
            return new FuncParam(bType, ident, null, Collections.emptyList());
        }
        Token RBk = getTokenIgnoreNull(TokenType.RBRACK, "<FuncFParam>", iterator, maxNum);
        if (!iterator.hasNext())
            return new FuncParam(bType, ident, new FirstDim(LBk, RBk), Collections.emptyList());
        List<ArrayDim> dims = new LinkedList<>();
        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.tokenType.equals(TokenType.LBRACK))
                dims.add(parseArrayDim(token));
            else {
                iterator.previous();
                break;
            }
        }
        return new FuncParam(bType, ident, new FirstDim(LBk, RBk), dims);
    }

    public FuncParams parseFuncParams()  {
        FuncParam first = parseFuncParam();
        List<Token> commas = new LinkedList<>();
        List<FuncParam> params = new LinkedList<>();
        while (iterator.hasNext()) {
            Token token = iterator.next();
            if (token.tokenType.equals(TokenType.COMMA)) {
                commas.add(token);
                params.add(parseFuncParam());
            } else {
                iterator.previous();
                break;
            }
        }
        return new FuncParams(first, commas, params);
    }

    public FuncDef parseFuncDef(Token type, Ident ident, Token LP)  {
        Token token = getTokenNext("<FuncDef>", iterator, maxNum);//token next LP
        if (token.tokenType.equals(TokenType.RPARENT)) { // no args
            Token LB = getTokenSpecial(TokenType.LBRACE, "<Block>", iterator, maxNum);
            return new FuncDef(new FuncType(type), ident, LP, null, token, new StmtParser(iterator, maxNum).parseBlock(LB));
        }
        //have args
        iterator.previous();
        if (token.tokenType.equals(TokenType.INTTK)){
            FuncParams funcParams = parseFuncParams();
            Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<FuncDef>", iterator, maxNum);
            Token LB = getTokenSpecial(TokenType.LBRACE, "<Block>", iterator, maxNum);
            return new FuncDef(new FuncType(type), ident, LP, funcParams, RP, new StmtParser(iterator, maxNum).parseBlock(LB));
        }
        //no args but don't have RP
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<FuncDef>", iterator, maxNum);
        Token LB = getTokenSpecial(TokenType.LBRACE, "<Block>", iterator, maxNum);
        return new FuncDef(new FuncType(type), ident, LP, null, RP, new StmtParser(iterator, maxNum).parseBlock(LB));
    }

    public MainFuncDef parseMainFuncDef(Token _int, Token _main, Token LP){
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<MainFuncDef>", iterator, maxNum);
        Token LB = getTokenSpecial(TokenType.LBRACE, "<Block>", iterator, maxNum);
        Block block = new StmtParser(iterator, maxNum).parseBlock(LB);
        return new MainFuncDef(_int, _main, LP, RP, block);
    }
}
