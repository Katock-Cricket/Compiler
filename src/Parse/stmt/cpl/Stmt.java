package Parse.stmt.cpl;

import Tokenize.token.Token;
import Parse.stmt.spl.SplStmt;

import java.io.PrintStream;
import java.util.Objects;

public class Stmt implements BlockItem{
    public final SplStmt splStmt;
    public final CplStmt cplStmt;
    public final Token divider; //";"
    public final StmtType stmtType;
    private final String name = "<Stmt>";

    public Stmt(SplStmt splStmt, CplStmt cplStmt, Token divider, StmtType stmtType){
        this.splStmt=splStmt;
        this.divider=divider;
        this.cplStmt=cplStmt;
        this.stmtType=stmtType;
    }

    @Override
    public void output(PrintStream p){
        if (stmtType.equals(StmtType.EMPTY) && Objects.nonNull(divider))
            divider.output(p);
        else if (stmtType.equals(StmtType.SIMPLE)) {
            splStmt.output(p);
            if (Objects.nonNull(divider))
                divider.output(p);
        }
        else
            cplStmt.output(p);
        p.println(name);
    }
}
