package Parse.expr.unary;

import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.ListIterator;

public class UnaryExp implements Printer {
    public UnaryOp unaryOp;
    public BaseUnaryExp baseUnaryExp;
    private final String name = "<UnaryExp>";

    public UnaryExp(UnaryOp op, BaseUnaryExp base){
        unaryOp = op;
        baseUnaryExp = base;
    }

    @Override
    public void output(PrintStream printStream){
        int depth=0;
        ListIterator<Token> it = unaryOp.ops.listIterator();
        while(it.hasNext()){
            Token op = it.next();
            op.output(printStream);
            printStream.println("<UnaryInstr>");
            depth++;
        }
        baseUnaryExp.output(printStream);
        printStream.println(name);
        while(depth-->0){
            printStream.println(name);
        }
    }
}
