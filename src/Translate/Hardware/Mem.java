package Translate.Hardware;

import java.util.*;
import java.util.stream.Collectors;

public class Mem {
    public final Map<Integer, String> constStr;
    public final Map<Integer, Integer> mem;//store not 0 vals

    public Mem(){
        constStr=new HashMap<>();
        mem=new TreeMap<>();
    }

    public int lw(int addr){
        return mem.getOrDefault(addr - (addr&0x3), 0);
    }

    public void sw(int addr, int val){
        int align = addr - (addr&0x3);
        if (val!=0)
            mem.put(align,val);
        else
            mem.remove(align);
    }

    public List<Integer> LowToHighAddr(){
        return mem.keySet().stream()
                .sorted(Integer::compareTo)
                .collect(Collectors.toList());
    }
}
