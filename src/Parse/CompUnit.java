package Parse;

import Parse.decl.Decl;
import Parse.func.FuncDef;
import Parse.func.MainFuncDef;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;

public class CompUnit implements Printer{
    public final List<Decl> decls;
    public final List<FuncDef> funcDefs;
    public final MainFuncDef mainFuncDef;

    public CompUnit(List<Decl> decls, List<FuncDef> funcDefs, MainFuncDef mainFuncDef){
        this.decls=decls;
        this.funcDefs=funcDefs;
        this.mainFuncDef=mainFuncDef;
    }

    @Override
    public void output(PrintStream p) {
        Iterator<Decl> declsIt = decls.iterator();
        Iterator<FuncDef> funcDefsIt = funcDefs.iterator();
        while(declsIt.hasNext())
            declsIt.next().output(p);
        while (funcDefsIt.hasNext())
            funcDefsIt.next().output(p);
        mainFuncDef.output(p);
        p.println("<CompUnit>");
    }
}
