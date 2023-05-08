package Parse;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Tokenize.token.TokenType;
import Parse.decl.Decl;
import Parse.decl.DeclParser;
import Parse.func.FuncDef;
import Parse.func.FuncParser;
import Parse.func.MainFuncDef;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import static Parse.ParserUtil.getTokenNext;

public class CompUnitParser {
    private final ListIterator<Token> iterator;
    private final int maxNum;
    private final String name = "<CompUnit>";

    public CompUnitParser(ListIterator iterator, int max) {
        this.iterator = iterator;
        maxNum = max;
    }

    public CompUnit parseCompUnit() {
        List<Decl> decls = new LinkedList<>();
        List<FuncDef> funcDefs = new LinkedList<>();
        MainFuncDef mainFuncDef = null;

        Token first = getTokenNext(name, iterator, maxNum);
        Token second = getTokenNext(name, iterator, maxNum);
        TokenType firstType;
        TokenType secondType;

        for (; iterator.hasNext();
             first = getTokenNext(name, iterator, maxNum),
                     second = getTokenNext(name, iterator, maxNum)) {

            firstType = first.tokenType;
            secondType = second.tokenType;
            if (firstType.equals(TokenType.CONSTTK) && secondType.equals(TokenType.INTTK)) // is constDef
                decls.add(new DeclParser(iterator, maxNum).parseDecl(first, second));
            else if (firstType.equals(TokenType.INTTK) && secondType.equals(TokenType.IDENFR))
                if (!(getTokenNext(name, iterator, maxNum).tokenType.equals(TokenType.LPARENT))) { // is varDef
                    iterator.previous();
                    decls.add(new DeclParser(iterator, maxNum).parseDecl(first, second));
                } else { // is funcDef(int)
                    iterator.previous();
                    break;
                }
            else //is mainFuncDef or funcDef(void)
                break;

        }

        Token third = getTokenNext(name, iterator, maxNum);
        TokenType thirdType;

        for (; iterator.hasNext();
             first = getTokenNext(name, iterator, maxNum),
                     second = getTokenNext(name, iterator, maxNum),
                     third = getTokenNext(name, iterator, maxNum)) {

            firstType = first.tokenType;
            secondType = second.tokenType;
            thirdType = third.tokenType;
            if (firstType.equals(TokenType.INTTK)
                    && secondType.equals(TokenType.MAINTK)
                    && thirdType.equals(TokenType.LPARENT)) {//is mainFuncDef
                mainFuncDef = new FuncParser(iterator, maxNum).parseMainFuncDef(first, second, third);
                break;
            } else {
                if (!firstType.equals(TokenType.INTTK) && !firstType.equals(TokenType.VOIDTK))
                    throw new Error("WrongToken: " + first.lineNumber + " " +"<FuncDef>"+" "+ first);
                if (!secondType.equals(TokenType.IDENFR))
                    throw new Error("WrongToken: " + second.lineNumber+ " " + "<FuncDef>"+" "+ second +" "+TokenType.IDENFR);
                if (!thirdType.equals(TokenType.LPARENT))
                    throw new Error("WrongToken: " + third.lineNumber+ " " +  "<FuncDef>"+" "+third+" "+ TokenType.IDENFR);
                funcDefs.add(new FuncParser(iterator, maxNum).parseFuncDef(first, (Ident) second, third));
            }

        }

        if (Objects.nonNull(mainFuncDef))
            return new CompUnit(decls, funcDefs, mainFuncDef);
        throw new Error(maxNum +" "+ "No main function");
    }
}
