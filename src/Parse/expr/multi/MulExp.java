package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.expr.unary.UnaryExp;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class MulExp implements Printer, MultiExp {
    private static final String name = "<MulExp>";
    public UnaryExp first;
    public List<Token> ops;
    public List<UnaryExp> operands;

    public MulExp(UnaryExp f, List<Token> tokenList, List<UnaryExp> unaryExpList) {
        first = f;
        ops = tokenList;
        operands = unaryExpList;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<UnaryExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
