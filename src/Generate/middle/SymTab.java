package Generate.middle;

import Generate.middle.operand.Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SymTab {
    private final String field;
    private final Map<String, Symbol> symbolMap = new HashMap<>();
    public int capacity;
    public final SymTab father;

    public SymTab(String field, SymTab symTab){
        this.field=field;
        this.father=symTab;
    }

    public void addSym(Symbol symbol){
        symbolMap.putIfAbsent(symbol.name, symbol);
        capacity += symbol.capacity();
    }

    public boolean contains(String name, boolean recursive){
        if (symbolMap.containsKey(name))
            return true;
        else if (Objects.nonNull(father) && recursive) {
            return father.contains(name, true);
        }
        else
            return false;
    }

    public Symbol getSym(String name, boolean recursive){
        Symbol symbol = symbolMap.get(name);
        if (Objects.nonNull(symbol))
            return symbol;
        if (recursive && Objects.nonNull(father))
            return father.getSym(name, true);
        return null;
    }
}
