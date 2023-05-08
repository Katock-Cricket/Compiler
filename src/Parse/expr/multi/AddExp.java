package Parse.expr.multi;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;

public class AddExp implements Printer, MultiExp {
    String name = "<AddExp>";
    public MulExp first;
    public List<Token> ops;
    public List<MulExp> operands;

    public AddExp(MulExp f, List<Token> tokenList, List<MulExp> unaryExpList){
        first=f;
        ops=tokenList;
        operands=unaryExpList;
    }
//    public MulExp(UnaryExp f, List<Token> tokenList, List<UnaryExp> unaryExpList){
//        super(f,tokenList,unaryExpList,name);
//    }

    @Override
    public void output(PrintStream p) {
        first.output(p);
        p.println(name);
        ListIterator<Token> opsIt = this.ops.listIterator();
        ListIterator<MulExp> operandsIt = this.operands.listIterator();
        while(opsIt.hasNext() && operandsIt.hasNext()){
            opsIt.next().output(p);
            operandsIt.next().output(p);
            p.println(name);
        }
    }
}
