package Parse.stmt;

import Tokenize.token.FormatString;
import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.decl.DeclParser;
import Parse.expr.ExpParser;

import Parse.expr.multi.Cond;
import Parse.expr.multi.Exp;
import Parse.expr.unary.*;
import Parse.stmt.cpl.*;
import Parse.stmt.spl.*;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static Parse.ParserUtil.*;

public class StmtParser {
    private final ListIterator<Token> iterator;
    private final int maxNum;

    public StmtParser(ListIterator<Token> tokenListIterator, int max){
        iterator=tokenListIterator;
        maxNum=max;
    }

    public ExpStmt parseExpStmt(Exp exp){
        return new ExpStmt(exp);
    }

    public BreakStmt parseBreakStmt(Token _break){
        return new BreakStmt(_break);
    }

    public ContinueStmt parseContinueStmt(Token _continue){
        return new ContinueStmt(_continue);
    }

    public ReturnStmt parseReturnStmt(Token _return) {
        Token token = getTokenNext("<ReturnStmt>", iterator, maxNum);
        iterator.previous();
        TokenType type = token.tokenType;
        if (type.equals(TokenType.IDENFR)
            || type.equals(TokenType.INTCON)
            || type.equals(TokenType.LPARENT)
            || type.equals(TokenType.PLUS)
            || type.equals(TokenType.MINU)
            || type.equals(TokenType.NOT))
            return new ReturnStmt(_return, new ExpParser(iterator, maxNum).parseExp());
        return new ReturnStmt(_return, null);
    }

    public AssignStmt parseAssignStmt(LVal lVal, Token assign) {
        return new AssignStmt(lVal, assign, new ExpParser(iterator, maxNum).parseExp());
    }

    public GetIntStmt parseGetIntStmt(LVal lVal, Token assign, Token _getInt)  {
        Token LP = getTokenSpecial(TokenType.LPARENT, "<InputStmt>", iterator, maxNum);
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<InputStmt>", iterator, maxNum);
        return new GetIntStmt(lVal, assign, _getInt, LP, RP);
    }

    public PrintStmt parsePrintStmt(Token printf) {
        Token LP = getTokenSpecial(TokenType.LPARENT, "<OutputStmt>", iterator, maxNum);
        FormatString formatString = (FormatString) getTokenSpecial(TokenType.STRCON, "<OutputStmt>", iterator, maxNum);
        List<Token> commas = new LinkedList<>();
        List<Exp> exps = new LinkedList<>();
        while(iterator.hasNext()){
            Token comma = iterator.next();
            if (comma.tokenType.equals(TokenType.COMMA)){
                commas.add(comma);
                exps.add(new ExpParser(iterator, maxNum).parseExp());
            }
            else{
                iterator.previous();
                break;
            }
        }
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<OutputStmt>", iterator, maxNum);
        return new PrintStmt(printf, LP, formatString, commas, exps, RP);
    }

    public SplStmt parseSplStmt(Token first) {
        TokenType type = first.tokenType;
        if (type.equals(TokenType.BREAKTK))
            return parseBreakStmt(first);
        else if (type.equals(TokenType.CONTINUETK))
            return parseContinueStmt(first);
        else if (type.equals(TokenType.RETURNTK))
            return parseReturnStmt(first);
        else if (type.equals(TokenType.PRINTFTK))
            return parsePrintStmt(first);
        else { // exp assign getint
            iterator.previous();
            Exp exp = new ExpParser(iterator, maxNum).parseExp();
            //exp: lval lval...
            //assign: lval = exp
            //getint: lval = getint
            LVal lVal;
            BaseUnaryExp baseUnaryExp = exp.addExp.first.first.baseUnaryExp;
            if (!(baseUnaryExp instanceof PrimaryExp)) {
                lVal = null;
            }
            else {
                PrimaryExp primaryExp = (PrimaryExp) baseUnaryExp;
                BasePrimaryExp basePrimaryExp = primaryExp.basePrimaryExp;
                if (!(basePrimaryExp instanceof LVal))
                    lVal=null;
                else if (exp.addExp.operands.size()+ exp.addExp.first.operands.size()+exp.addExp.first.first.unaryOp.ops.size()==0){
                    lVal = (LVal) basePrimaryExp;
                }
                else{
                    lVal=null;
                }
            }

            if (Objects.nonNull(lVal)){
                Token token = getTokenNext("<SplStmt>",iterator, maxNum);
                if (token.tokenType.equals(TokenType.ASSIGN)){
                    Token token1 = getTokenNext("<SplStmt>", iterator, maxNum);
                    if (token1.tokenType.equals(TokenType.GETINTTK))
                        return parseGetIntStmt(lVal, token, token1);
                    else {
                        iterator.previous();
                        return parseAssignStmt(lVal, token);
                    }

                }else
                    iterator.previous();
            }
            return parseExpStmt(exp);
        }
    }

    public IfStmt parseIfStmt(Token _if) {
        Token LP = getTokenSpecial(TokenType.LPARENT, "<IfStmt>", iterator, maxNum);
        Cond cond = new ExpParser(iterator, maxNum).parseCond();
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<IfStmt>", iterator, maxNum);
        Stmt stmt1 = parseStmt();
        Token _else;
        if (iterator.hasNext() && (_else=iterator.next()).tokenType.equals(TokenType.ELSETK))
            return new IfStmt(_if, LP, cond, RP, stmt1, _else, parseStmt());
        else {
            iterator.previous();
            return new IfStmt(_if, LP, cond, RP, stmt1, null, null);
        }
    }

    public WhileStmt parseWhileStmt(Token _while) {
        Token LP = getTokenSpecial(TokenType.LPARENT, "<WhileStmt>", iterator, maxNum);
        Cond cond = new ExpParser(iterator,maxNum).parseCond();
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<WhileStmt>", iterator, maxNum);
        return new WhileStmt(_while, LP, cond, RP, parseStmt());
    }

    public BlockItem parseBlockItem() {
        Token token = getTokenNext("<BlockItem>",iterator,maxNum);
        TokenType type = token.tokenType;
        if (type.equals(TokenType.INTTK))
            return new DeclParser(iterator, maxNum).parseDecl(token, getTokenSpecial(TokenType.IDENFR, "<Decl>", iterator, maxNum));
        if (type.equals(TokenType.CONSTTK))
            return new DeclParser(iterator, maxNum).parseDecl(token, getTokenSpecial(TokenType.INTTK, "<Decl>", iterator, maxNum));
        iterator.previous();
        return parseStmt();
    }

     public Block parseBlock(Token LB) {
        List<BlockItem> blockItems = new LinkedList<>();
        while (iterator.hasNext()){
            Token RB = iterator.next();
            if (RB.tokenType.equals(TokenType.RBRACE))
                return new Block(LB, blockItems, RB);
            iterator.previous();
            blockItems.add(parseBlockItem());
        }
        throw new Error("Unexpected EOF: " + maxNum);
     }

    public CplStmt parseCplStmt(Token first) {
        TokenType type = first.tokenType;
        if (type.equals(TokenType.IFTK))
            return parseIfStmt(first);
        else if (type.equals(TokenType.LBRACE))
            return parseBlock(first);
        else if (type.equals(TokenType.WHILETK))
            return parseWhileStmt(first);
        else
            throw new Error("WrongToken: " + first.lineNumber+ "<CplStmt>"+ first);
    }

    public Stmt parseStmt() {
        Token token = getTokenNext("<Stmt>", iterator, maxNum);
        TokenType type = token.tokenType;
        switch (type) {
            case SEMICN:
                return new Stmt(null,null,token,StmtType.EMPTY);
            case LBRACE:
            case IFTK:
            case WHILETK:
                return new Stmt(null, parseCplStmt(token),null,StmtType.COMPLEX);
            default:
                return new Stmt(parseSplStmt(token), null, getTokenIgnoreNull(TokenType.SEMICN, "<Stmt>", iterator,maxNum),StmtType.SIMPLE);
        }
    }
}
