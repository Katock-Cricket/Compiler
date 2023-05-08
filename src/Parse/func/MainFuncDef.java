package Parse.func;

import Tokenize.token.Token;
import Parse.Printer;
import Parse.stmt.cpl.Block;

import java.io.PrintStream;
import java.util.Objects;

public class MainFuncDef implements Printer {
    private final Token _int, _main, LP, RP;
    public final Block block;

    public MainFuncDef(Token _int, Token _main, Token LP, Token RP, Block block){
        this._int=_int;
        this._main=_main;
        this.LP=LP;
        this.RP=RP;
        this.block=block;
    }

    @Override
    public void output(PrintStream p) {
        _int.output(p);
        _main.output(p);
        LP.output(p);
        if (Objects.nonNull(RP))
            RP.output(p);
        block.output(p);
        p.println("<MainFuncDef>");
    }
}
