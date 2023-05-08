package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class RelExp implements Printer, MultiExp {
    private final static String name = "<RelExp>";
    public AddExp first;
    public List<Token> ops;
    public List<AddExp> operands;

    public RelExp(AddExp f, List<Token> tokenList, List<AddExp> addExpList) {
        first=f;
        ops=tokenList;
        operands=addExpList;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<AddExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
