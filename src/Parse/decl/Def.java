package Parse.decl;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Parse.Printer;

import java.io.PrintStream;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public class Def implements Printer {
    public final boolean isConst;
    public final Ident ident;
    public final List<ArrayDef> arrayDefs;
    private final Token assign;
    public final InitVal initVal;

    public Def(boolean isConst, Ident ident, List<ArrayDef> arrayDefs, Token assign, InitVal initVal){
        this.isConst=isConst;
        this.ident=ident;
        this.arrayDefs=arrayDefs;
        this.assign=assign;
        this.initVal=initVal;
    }

    public boolean isInitialized(){
        return Objects.nonNull(assign) && Objects.nonNull(initVal);
    }

    @Override
    public void output(PrintStream p){
        ident.output(p);
        ListIterator<ArrayDef> it= arrayDefs.listIterator();
        while(it.hasNext())
            it.next().output(p);
        if (Objects.nonNull(assign))
            assign.output(p);
        if (Objects.nonNull(initVal))
            initVal.output(p);
        if (isConst)
            p.println("<ConstDef>");
        else
            p.println("<VarDef>");
    }
}
