package Parse.decl;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.ParserUtil;
import Parse.expr.ExpParser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static Parse.ParserUtil.getTokenSpecial;
import static Parse.ParserUtil.getTokenNext;

public class DeclParser {
    private final ListIterator<Token> iterator;
    private final int maxNum;

    public DeclParser(ListIterator<Token> it, int max){
        iterator=it;
        maxNum=max;
    }

    public ExpInitVal parseExpInitVal(boolean isConst) {
        if (isConst)
            return new ExpInitVal(true, new ExpParser(iterator, maxNum).parseConstExp());
        return new ExpInitVal(false, new ExpParser(iterator, maxNum).parseExp());
    }

    public ArrayDef parseArrayDef(Token LBk) {
        return new ArrayDef(LBk, new ExpParser(iterator,maxNum).parseConstExp(), ParserUtil.getTokenIgnoreNull(TokenType.RBRACK, "<ArrDef>",iterator,maxNum));
    }

    public Def parseDef(boolean isConst, Ident ident){
        List<ArrayDef> arrayDefs = new LinkedList<>();
        while (iterator.hasNext()){
            Token LBk = iterator.next();
            if (LBk.tokenType.equals(TokenType.LBRACK))
                arrayDefs.add(parseArrayDef(LBk));
            else {
                iterator.previous();
                break;
            }
        }
        Token assign = iterator.next();
        if (assign.tokenType.equals(TokenType.ASSIGN))
            return new Def(isConst, ident, arrayDefs, assign, parseInitVal(isConst));
        iterator.previous();
        return new Def(false, ident, arrayDefs, null, null);
    }

    public ArrayInitVal parseArrayInitVal(boolean isConst, Token LB){
        Token RB = getTokenNext("<ArrInitVal>",iterator,maxNum);
        if (RB.tokenType.equals(TokenType.RBRACE))
            return new ArrayInitVal(isConst, LB, null, Collections.emptyList(), Collections.emptyList(), RB);
        iterator.previous();
        InitVal first = parseInitVal(isConst);
        List<Token> commas = new LinkedList<>();
        List<InitVal> vals = new LinkedList<>();
        Token token = null;
        while(iterator.hasNext()){
            token = iterator.next();
            TokenType type = token.tokenType;
            if (type.equals(TokenType.RBRACE))
                return new ArrayInitVal(isConst, LB, first, commas, vals, token);
            if (type.equals(TokenType.COMMA)){
                commas.add(token);
                vals.add(parseInitVal(isConst));
            }else {
                throw new Error("WrongToken: " + token.lineNumber +" "+ "<ArrInitVal>"+" "+ token);
            }
        }
        return new ArrayInitVal(isConst, LB, first, commas, vals, token);
    }

    public InitVal parseInitVal(boolean isConst) {
        Token token = getTokenNext("<InitVal>",iterator,maxNum);
        if (token.tokenType.equals(TokenType.LBRACE))
            return parseArrayInitVal(isConst, token);
        iterator.previous();
        return parseExpInitVal(isConst);
    }

    public Decl parseDecl(Token token1, Token token2) {
        Token _const, bType;
        String name;
        Ident ident;
        boolean isConst;
        TokenType type1 = token1.tokenType, type2 = token2.tokenType;
        if (type1.equals(TokenType.CONSTTK) && type2.equals(TokenType.INTTK)) {
            _const = token1;
            bType = token2;
            isConst = true;
            name = "<ConstDecl>";
            ident = (Ident) getTokenSpecial(TokenType.IDENFR, name, iterator, maxNum);
        } else if (type1.equals(TokenType.INTTK) && type2.equals(TokenType.IDENFR)) {
            isConst = false;
            _const=null;
            bType = token1;
            ident = (Ident) token2;
            name = "<VarDecl>";
        } else
            throw new Error("WrongToken: " + token1.lineNumber+" "+ "<Decl>"+" "+ token1);

        Def first = parseDef(isConst, ident);
        List<Token> commas = new LinkedList<>();
        List<Def> defs = new LinkedList<>();
        Token semi=null;
        while (iterator.hasNext()) {
            Token token = iterator.next();
            TokenType type = token.tokenType;
            if (type.equals(TokenType.COMMA)) {
                commas.add(token);
                defs.add(parseDef(isConst, (Ident) getTokenSpecial(TokenType.IDENFR, name, iterator, maxNum)));
            } else if (type.equals(TokenType.SEMICN)) {
                semi = token;
                break;
            } else {
                iterator.previous();
                break;
            }
        }
        return new Decl(_const, bType, first, commas, defs, semi);
    }
}
