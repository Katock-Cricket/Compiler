package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class LOrExp implements Printer, MultiExp {
    private static final String name="<LOrExp>";
    public LAndExp first;
    public List<Token> ops;
    public List<LAndExp> operands;

    public LOrExp(LAndExp f, List<Token> tokenList, List<LAndExp> lAndExpList){
        first=f;
        ops=tokenList;
        operands=lAndExpList;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<LAndExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
