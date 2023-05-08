package Translate;

import Translate.Hardware.Mem;
import Translate.Instruction.Instruction;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Mips {
    public static final int STRING_START_ADDR = 0x10000000,
    DATA_START_ADDR = 0x10008000;
    public final Map<String,String> constStr;
    public final Map<String,Integer> constStrAddr;
    public int constStrSize;
    public final Mem mem;
    public String label, description;
    public final Instruction entry, tail; // is always nop

    public Mips(){
        constStr=new HashMap<>();
        constStrAddr=new HashMap<>();
        constStrSize=0;
        mem=new Mem();
        label=null;
        description="";
        entry=Instruction.nop();
        tail=Instruction.nop();
        tail.prev=entry;
        entry.next=tail;
    }

    public void addConstStr(String label, String content){
        constStr.put(label, content);
        constStrAddr.put(label, constStrSize);
        mem.constStr.put(STRING_START_ADDR+constStrSize,content);
        String replaceEscape = content.replace("\\n","X");
        constStrSize += replaceEscape.length()+1;
    }

    public void append(Instruction... instructions){
        for(Instruction instruction : instructions)
        {
            if (Objects.isNull(instruction))
                break;
            if (Objects.nonNull(label)){
                instruction.label=label;
                label = null;
            }
            if (!description.isEmpty()){
                instruction.description=description;
                description="";
            }
            Instruction last = (Instruction) tail.prev;
            last.next = instruction;
            instruction.prev = last;
            Instruction newTail = instruction;
            while (Objects.nonNull(newTail.next))
                newTail = (Instruction) newTail.next;
            newTail.next = tail;
            tail.prev = newTail;
        }
    }

    public void output(PrintStream p){
        p.printf(".data 0x%x\n", STRING_START_ADDR);
        for(Map.Entry<String,String> entry : constStr.entrySet())
            p.printf(".asciiz \"%s\"\n",entry.getValue());

        p.printf(".data 0x%x\n", DATA_START_ADDR);
        p.print(".word ");
        int lastAddr = -4;
        for(int addr : mem.LowToHighAddr()){
            if (addr - lastAddr > 4){
                p.printf("\n.space %d\n", addr-lastAddr-4);
                p.print(".word ");
            }
            p.printf("%d ", mem.lw(addr));
            lastAddr = addr;
        }

        p.print("\n.text\n");
        Instruction instruction = (Instruction) entry.next;
        while (Objects.nonNull(instruction.next)){
            p.println(instruction);
            instruction = (Instruction) instruction.next;
        }
    }
}
