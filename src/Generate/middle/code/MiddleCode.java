package Generate.middle.code;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class MiddleCode {
    public final Map<String, Integer> globalAddr;
    public final Map<String, Integer> globalVar;
    public final Map<String, List<Integer>> globalArr;
    public final Map<String, String> globalStr;
    public final Map<String, FuncScope> func;
    public FuncScope mainFunc;
    private int strCnt;

    public MiddleCode(){
        func=new HashMap<>();
        globalAddr=new HashMap<>();
        globalArr=new HashMap<>();
        globalStr=new HashMap<>();
        globalVar=new HashMap<>();
        strCnt=0;
    }

    public void addGlobalVar(String name, int var, int addr){
        globalVar.put(name, var);
        globalAddr.put(name, addr);
    }

    public void addGlobalArr(String name, List<Integer> vals, int addr){
        globalArr.put(name, vals);
        globalAddr.put(name, addr);
    }

    public String addGlobalStr(String s){
        String label = "STR_" + (++strCnt);
        globalStr.put(label, s);
        return label;
    }

    public void addFunc(FuncScope funcScope){
        func.put(funcScope.name, funcScope);
    }

    public void addMainFunc(FuncScope main){
        mainFunc=main;
    }

    public void output(PrintStream p){
        p.println("======= IR =======");
        p.println("\n== Global Variables ==");
        for(Map.Entry<String, Integer> entry : globalVar.entrySet().stream()
                .sorted(Comparator.comparingInt(entry1 -> globalAddr.get(entry1.getKey())))
                .collect(Collectors.toList())) { // sort by addr from low to high
            p.printf("%s[0x%x]: %d\n",
                    entry.getKey(), globalAddr.get(entry.getKey()), entry.getValue());
        }

        p.println(("\n== Global Arrays =="));
        for(Map.Entry<String, List<Integer>> entry : globalArr.entrySet().stream()
                .sorted(Comparator.comparingInt(entry1 -> globalAddr.get(entry1.getKey())))
                .collect(Collectors.toList())) {
            p.printf("%s[0x%x]: [%s]\n",
                    entry.getKey(),
                    globalAddr.get(entry.getKey()),
                    entry.getValue().stream().map(Object::toString).reduce((s,s2)->s+", "+s2).orElse(""));
        }

        p.println("\n== Global Strings ==");
        for(Map.Entry<String, String> entry : globalStr.entrySet())
            p.printf("%s: \"%s\"\n", entry.getKey(), entry.getValue());

        p.println("\n== Text ==\n");
        HashSet<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();
        for(FuncScope func : func.values()){
            p.printf("# Function %s: stack size = 0x%x\n", func.name, func.stackTop);
            queue.offer(func.body);
            while (!queue.isEmpty()){
                BasicBlock front = queue.poll();
                if (visited.contains(front))
                    continue;
                visited.add(front);
                p.println(front.label + ":");
                Node node = front.next;
                while (Objects.nonNull(node) && Objects.nonNull(node.next)){
                    if (node instanceof Jump){
                        queue.offer(((Jump) node).target);
                    }
                    else if (node instanceof Branch){
                        queue.offer(((Branch) node).thenBlock);
                        queue.offer(((Branch) node).elseBlock);
                    }
                    p.println("    " + node);
                    node = node.next;
                }
                p.println();
            }
        }
    }
}
