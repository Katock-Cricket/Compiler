package Parse.decl;

import Tokenize.token.Token;
import Parse.stmt.cpl.BlockItem;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Decl implements BlockItem {
    public final Token _const, bType;
    public final Def first;
    private final List<Token> commas;
    public final List<Def> defs;
    public final Token divider;
    final boolean isConst;

    public Decl(Token _const, Token bType, Def first, List<Token> commas, List<Def> defs, Token divider){
        this._const=_const;
        this.bType=bType;
        this.first=first;
        this.commas=commas;
        this.defs=defs;
        this.divider=divider;
        isConst=Objects.nonNull(_const);
    }

    @Override
    public void output(PrintStream p){
        String name = "<VarDecl>";
        if (Objects.nonNull(_const)) {
            name = "<ConstDecl>";
            _const.output(p);
        }
        bType.output(p);
        first.output(p);
        Iterator<Token> commasIt = commas.iterator();
        Iterator<Def> defsIt = defs.iterator();
        while(commasIt.hasNext()){
            commasIt.next().output(p);
            defsIt.next().output(p);
        }
        if (Objects.nonNull(divider))
            divider.output(p);
        p.println(name);
    }
}
