package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class EqExp implements Printer, MultiExp {
    private static final String name = "<EqExp>";
    public RelExp first;
    public List<Token> ops;
    public List<RelExp> operands;
    public EqExp(RelExp f, List<Token> tokenList, List<RelExp> relExpList){
        first=f;
        ops=tokenList;
        operands=relExpList;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<RelExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
