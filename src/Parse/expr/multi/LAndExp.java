package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class LAndExp implements Printer,MultiExp {
    private static final String name = "<LAndExp>";
    public EqExp first;
    public List<Token> ops;
    public List<EqExp> operands;

    public LAndExp(EqExp f, List<Token> tokenList, List<EqExp> eqExpList){
        first=f;
        ops=tokenList;
        operands=eqExpList;
    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<EqExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
