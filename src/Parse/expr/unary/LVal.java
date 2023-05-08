package Parse.expr.unary;

import Tokenize.token.Ident;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class LVal implements BasePrimaryExp {
    public final Ident ident;
    public List<Index> indexList;

    public LVal(Ident ident, List<Index> indexList1){
        this.ident =ident;
        indexList=indexList1;
    }

    @Override
    public void output(PrintStream p){
        ident.output(p);
        Iterator<Index> indexIterator = indexList.iterator();
        while (indexIterator.hasNext())
            indexIterator.next().output(p);
        p.println("<LVal>");
    }
}
