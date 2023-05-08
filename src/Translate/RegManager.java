package Translate;

import Generate.middle.operand.Symbol;
import Translate.Hardware.Reg;

import java.util.*;

public class RegManager {
    public final Set<Reg> allocatableRegs;
    public final Set<Reg> freeRegs;
    public final TreeMap<Reg, Symbol> allocatedRegs;
    public final Map<Symbol, Reg> symRegMap; // same as allocatedRegs, allow search by Symbol
    public final LinkedHashSet<Reg> cache;

    public RegManager(){
        allocatableRegs = new TreeSet<>();
        for(Reg reg : Reg.values())
            if (reg.id>=5 && reg.id<=25)
                allocatableRegs.add(reg);
        freeRegs = new TreeSet<>();
        freeRegs.addAll(allocatableRegs);
        allocatedRegs=new TreeMap<>();
        symRegMap=new HashMap<>();
        cache=new LinkedHashSet<>();
    }

    public boolean isAllocated(Symbol symbol){
        return symRegMap.containsKey(symbol);
    }

    public boolean isAllocated(Reg reg){
        return allocatedRegs.containsKey(reg);
    }

    public Reg allocReg(Symbol symbol){
        if (isAllocated(symbol))
            return symRegMap.get(symbol);
        Reg reg = freeRegs.iterator().next();
        freeRegs.remove(reg);
        allocatedRegs.put(reg, symbol);
        symRegMap.put(symbol,reg);
        cache.add(reg);
        return reg;
    }

    public Reg getReg(Symbol symbol){
        if (!isAllocated(symbol))
            throw new AssertionError(symbol.toString()+" is not allocated to reg");
        return symRegMap.get(symbol);
    }

    public Reg regToReplace(){
        if (cache.isEmpty())
            return null;
        return cache.iterator().next();
    }

    public Symbol free(Reg reg){
        if (!isAllocated(reg))
            return null;
        freeRegs.add(reg);
        Symbol symbol = allocatedRegs.remove(reg);
        symRegMap.remove(symbol);
        cache.remove(reg);
        return symbol;
    }

    public void clear(){
        allocatedRegs.clear();
        symRegMap.clear();
        cache.clear();
        freeRegs.clear();
        freeRegs.addAll(allocatableRegs);
    }

    public void updateCache(Symbol symbol){ // LRU
        Reg reg = symRegMap.get(symbol);
        cache.remove(reg);
        cache.add(reg);
    }
}
