package Parse.expr;

import Tokenize.token.Ident;
import Tokenize.token.IntConst;
import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.ParserUtil;
import Parse.expr.multi.*;
import Parse.expr.unary.Number;
import Parse.expr.unary.*;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static Parse.ParserUtil.getTokenNext;
import static Parse.ParserUtil.getTokenIgnoreNull;

public class ExpParser {
    private final ListIterator<Token> iterator;
    private final int maxNum;

    public ExpParser(ListIterator<Token> it, int max){
        iterator = it;
        maxNum = max;
    }

    private boolean handlePlusMinu(List<Token> ops) {
        Token op = iterator.next();
        TokenType tokenType = op.tokenType;
        if (!(tokenType.equals(TokenType.PLUS)
                || tokenType.equals(TokenType.MINU) || tokenType.equals(TokenType.NOT))){
            iterator.previous();
            return true;
        }
        ops.add(op);
        return false;
    }

    public SubExp parseSubExp(Token LP) {
        Exp exp = parseExp();
        Token RP = getTokenIgnoreNull(TokenType.RPARENT, "<SubExp>", iterator, maxNum);
        return new SubExp(LP, RP, exp);
    }

    public FuncCall parseFuncCall(Ident ident, Token LP) {
        //look forward 1 step
        Token next = getTokenNext("<UnaryExp>",iterator,maxNum);
        TokenType tokenType = next.tokenType;
        //no params
        if (tokenType.equals(TokenType.RPARENT))
            return new FuncCall(ident, LP, next, null);
        //with params but not
        if (!(tokenType.equals(TokenType.IDENFR) //not Ident
            || tokenType.equals(TokenType.LPARENT) //not (
            || tokenType.equals(TokenType.INTCON) //not const int
            || tokenType.equals(TokenType.PLUS) //not +
            || tokenType.equals(TokenType.MINU) //not -
            || tokenType.equals(TokenType.NOT))){ //not !
            iterator.previous();
            return new FuncCall(ident, LP, null, null);
        }
        //with params
        iterator.previous();
        FuncRParams funcRParams = parseFuncRParams(parseExp());
        return new FuncCall(ident, LP,
                getTokenIgnoreNull(TokenType.RPARENT, "<UnaryExp>", iterator, maxNum),
                funcRParams);
    }

    public Number parseNumber(IntConst intConst){
        return new Number(intConst);
    }

    public LVal parseLVal(Ident ident)  {
        List<Index> indexList = new LinkedList<>();//linked is better for add and del
        while(iterator.hasNext()){
            //look forward 1 step, get '['
            Token LBk = iterator.next();
            if(!(LBk.tokenType.equals(TokenType.LBRACK))) {
                iterator.previous();
                break;
            }
            Exp exp = parseExp(); // get index value
            Token RBk = getTokenIgnoreNull(TokenType.RBRACK, "<LVal>", iterator, maxNum); //get ']'
            indexList.add(new Index(LBk, RBk, exp));
        }
        return new LVal(ident, indexList);
    }

    public FuncRParams parseFuncRParams(Exp firstParam) {
        List<Exp> params = new LinkedList<>();
        List<Token> commas = new LinkedList<>();
        while (iterator.hasNext()){
            Token token = iterator.next();
            if (!(TokenType.COMMA.equals(token.tokenType))){
                iterator.previous();
                break;
            }
            // no conditions like 'func(param,)'
            ParserUtil.handleEof("<FuncRParams>", iterator, maxNum);
            commas.add(token);
            params.add(parseExp());
        }
        return new FuncRParams(firstParam, commas, params);
    }

    public PrimaryExp parsePrimaryExp(Token first) {
        ParserUtil.handleEof("<PrimaryExp>", iterator, maxNum);
        TokenType tokenType = first.tokenType;//already looked forward

        if (tokenType.equals(TokenType.IDENFR))
            return new PrimaryExp(parseLVal((Ident)first));
        if (tokenType.equals(TokenType.INTCON))
            return new PrimaryExp(parseNumber((IntConst) first));
        if (tokenType.equals(TokenType.LPARENT))
            return new PrimaryExp(parseSubExp(first));
        throw new Error("WrongToken: " + first.lineNumber+ "<PrimaryExp>"+ first);
    }

    public BaseUnaryExp parseBaseUnaryExp() {
        Token token = getTokenNext("<UnaryExp>", iterator, maxNum);
        TokenType tokenType = token.tokenType;
        if (!(tokenType.equals(TokenType.IDENFR))){ //not ident, must be primary
            return parsePrimaryExp(token);
        }
        //funcCall or LVal(Primary)
        Token token1 = getTokenNext("<UnaryExp>", iterator, maxNum);
        tokenType = token1.tokenType;
        if (tokenType.equals(TokenType.LPARENT))// funcCall
            return parseFuncCall((Ident) token, token1);
        iterator.previous(); //Primary
        return parsePrimaryExp(token);
    }

    public UnaryExp parseUnaryExp() {
        List<Token> ops = new LinkedList<>();
        while(iterator.hasNext()) {
            Token token = iterator.next();
            TokenType tokenType= token.tokenType;
            if (!(tokenType.equals(TokenType.PLUS)
                    || tokenType.equals(TokenType.MINU)
                    || tokenType.equals(TokenType.NOT))) {
                break;
            }
            ops.add(token);
        }
        iterator.previous();
        return new UnaryExp(new UnaryOp(ops), parseBaseUnaryExp());
    }

    public MulExp parseMulExp() {
        UnaryExp first = parseUnaryExp();
        List<Token> ops = new LinkedList<>();
        List<UnaryExp> operands = new LinkedList<>();
        while (iterator.hasNext()){
            Token op = iterator.next();
            TokenType tokenType = op.tokenType;
            if (!(tokenType.equals(TokenType.MULT)
                || tokenType.equals(TokenType.DIV)
                || tokenType.equals(TokenType.MOD))){
                iterator.previous();
                break;
            }
            ops.add(op);
            operands.add(parseUnaryExp());
        }
        return new MulExp(first, ops, operands);
    }

    public AddExp parseAddExp() {
        MulExp first = parseMulExp();
        List<Token> ops = new LinkedList<>();
        List<MulExp> operands = new LinkedList<>();
        while (iterator.hasNext()){
            if (handlePlusMinu(ops)) break;
            operands.add(parseMulExp());
        }
        return new AddExp(first, ops, operands);
    }

    public Exp parseExp(){
        return new Exp(parseAddExp());
    }

    public ConstExp parseConstExp() {
        return new ConstExp(parseAddExp());
    }

    public RelExp parseRelExp() {
        AddExp first = parseAddExp();
        List<Token> ops = new LinkedList<>();
        List<AddExp> operands = new LinkedList<>();
        while (iterator.hasNext()){
            Token op = iterator.next();
            TokenType tokenType = op.tokenType;
            if (!(tokenType.equals(TokenType.LSS)
                || tokenType.equals(TokenType.GRE)
                || tokenType.equals(TokenType.LEQ)
                || tokenType.equals(TokenType.GEQ))){
                iterator.previous();
                break;
            }
            ops.add(op);
            operands.add(parseAddExp());
        }
        return new RelExp(first, ops, operands);
    }

    public EqExp parseEqExp() {
        RelExp first = parseRelExp();
        List<Token> ops = new LinkedList<>();
        List<RelExp> operands = new LinkedList<>();
        while(iterator.hasNext()){
            Token op = iterator.next();
            TokenType tokenType = op.tokenType;
            if (!(tokenType.equals(TokenType.EQL)
                || tokenType.equals(TokenType.NEQ))){
                iterator.previous();
                break;
            }
            ops.add(op);
            operands.add(parseRelExp());
        }
        return new EqExp(first, ops, operands);
    }

    public LAndExp parseLAndExp() {
        EqExp first = parseEqExp();
        List<Token> ops = new LinkedList<>();
        List<EqExp> operands = new LinkedList<>();
        while (iterator.hasNext()){
            Token op = iterator.next();
            TokenType tokenType = op.tokenType;
            if (!(tokenType.equals(TokenType.AND))){
                iterator.previous();
                break;
            }
            ops.add(op);
            operands.add(parseEqExp());
        }
        return new LAndExp(first, ops, operands);
    }

    public LOrExp parseLOrExp()  {
        LAndExp first = parseLAndExp();
        List<Token> ops = new LinkedList<>();
        List<LAndExp> operands = new LinkedList<>();
        while (iterator.hasNext()) {
            Token op = iterator.next();
            if (!(TokenType.OR.equals(op.tokenType))) {
                iterator.previous();
                break;
            }
            ops.add(op);
            operands.add(parseLAndExp());
        }
        return new LOrExp(first, ops, operands);
    }

    public Cond parseCond() {
        return new Cond(parseLOrExp());
    }
}
