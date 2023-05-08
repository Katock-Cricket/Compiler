package Parse.decl;

import Tokenize.token.Token;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class ArrayInitVal implements InitVal{
    public final boolean isConst;
    private final Token LB, RB;
    public final InitVal first;
    private final List<Token> commas;
    public final List<InitVal> vals;

    public ArrayInitVal(boolean isConst, Token LB, InitVal first, List<Token> commas, List<InitVal> vals, Token RB){
        this.isConst=isConst;
        this.LB=LB;
        this.first=first;
        this.commas=commas;
        this.vals=vals;
        this.RB=RB;
    }

    @Override
    public void output(PrintStream p){
        LB.output(p);
        if (Objects.nonNull(first)){
            first.output(p);
            if (!commas.isEmpty() && !vals.isEmpty()){
                Iterator<Token> commaIt = commas.iterator();
                Iterator<InitVal> valIt = vals.iterator();
                while(commaIt.hasNext()){
                    commaIt.next().output(p);
                    valIt.next().output(p);
                }
            }
        }
        if (Objects.nonNull(RB))
            RB.output(p);
        if (isConst)
            p.println("<ConstInitVal>");
        else
            p.println("<InitVal>");
    }
}
