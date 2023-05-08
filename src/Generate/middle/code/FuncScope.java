package Generate.middle.code;

import Generate.middle.SymTab;
import Generate.middle.operand.Symbol;

import java.util.ArrayList;
import java.util.List;

public class FuncScope {
    public final String name;
    public final String label;
    public final SymTab symTab; // fatherTab
    public final List<Symbol> params;
    public int stackTop;
    public BasicBlock body;
    public final boolean isMain;
    public final ReturnType retType;

    public FuncScope(String name, ReturnType retType, SymTab symTab){
        this.name=name;
        this.label="FUNC_"+name;
        this.retType=retType;
        this.symTab =new SymTab(name, symTab);
        this.isMain= name.equals("main");
        params=new ArrayList<>();
        stackTop = 0;
    }

    public static FuncScope Function(String name, ReturnType refType, SymTab global){
        return new FuncScope(name, refType, global);
    }

    public static FuncScope MainFunction(SymTab symTab){
        return new FuncScope("main", ReturnType.INT, symTab);
    }

    public void updateStackTop(int size){
        stackTop=stackTop>size?stackTop:size;
    }
}
