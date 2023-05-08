package Parse.stmt.cpl;

import Tokenize.token.Token;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class Block implements CplStmt{
    public final Token LB, RB;
    private final List<BlockItem> blockItems;
    private final String name = "<Block>";

    public Block(Token LB, List<BlockItem> blockItems, Token RB){
        this.LB=LB;
        this.blockItems=blockItems;
        this.RB=RB;
    }

    public Iterator<BlockItem> blockItemIt(){
        return blockItems.iterator();
    }

    @Override
    public void output(PrintStream p){
        LB.output(p);
        Iterator<BlockItem>it= blockItemIt();
        while(it.hasNext())
            it.next().output(p);
        RB.output(p);
        p.println("<Block>");
    }

}
