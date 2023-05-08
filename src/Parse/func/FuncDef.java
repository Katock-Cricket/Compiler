package Parse.func;

import Tokenize.token.Ident;
import Tokenize.token.Token;
import Parse.Printer;
import Parse.decl.FuncType;
import Parse.stmt.cpl.Block;

import java.io.PrintStream;
import java.util.Objects;

public class FuncDef implements Printer {
    public final FuncType funcType;
    public final Ident ident;
    public final Token LP, RP;
    public final FuncParams params;
    public final Block block;

    public FuncDef(FuncType funcType, Ident ident, Token LP, FuncParams params, Token RP, Block block){
        this.funcType=funcType;
        this.ident=ident;
        this.LP=LP;
        this.params=params;
        this.RP=RP;
        this.block=block;
    }

    @Override
    public void output(PrintStream p) {
        funcType.output(p);
        ident.output(p);
        LP.output(p);
        if (Objects.nonNull(params))
            params.output(p);
        if (Objects.nonNull(RP))
            RP.output(p);
        block.output(p);
        p.println("<FuncDef>");
    }
}
