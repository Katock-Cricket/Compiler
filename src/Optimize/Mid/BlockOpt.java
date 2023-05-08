package Optimize.Mid;

import Generate.middle.code.*;

import java.util.*;

public class BlockOpt implements MidOpt{
    private final Map<BasicBlock, Integer> blockRes;

    public BlockOpt(){
        blockRes = new HashMap<>();
    }

    @Override
    public void method(Node node, Queue<BasicBlock> queue) {
        if (node instanceof Jump){
            queue.offer(((Jump) node).target);
            blockRes.merge(((Jump) node).target, 1, Integer::sum);
        }
        else if (node instanceof Branch){
            queue.offer(((Branch) node).thenBlock);
            queue.offer(((Branch) node).elseBlock);
            blockRes.merge(((Branch) node).thenBlock, 1, Integer::sum);
            blockRes.merge(((Branch) node).elseBlock, 1, Integer::sum);
        }
    }

    @Override
    public void optimize(MiddleCode middleCode) {
        //count BlockRes
        HashSet<BasicBlock> visited = new HashSet<>();
        Queue<BasicBlock> queue = new LinkedList<>();
        for(FuncScope func : middleCode.func.values()) {
            queue.offer(func.body);
            while (!queue.isEmpty()) {
                BasicBlock block = queue.poll();
                if (visited.contains(block)) continue;
                visited.add(block);
                for (Node node = block.next; Objects.nonNull(node) && Objects.nonNull(node.next); node = node.next) {
                    method(node, queue);
                }
            }
        }
        //if a block only has one res, merge res and block
        visited.clear();
        for(FuncScope func : middleCode.func.values()) {
            queue.offer(func.body);
            while (!queue.isEmpty()) {
                BasicBlock block = queue.poll();
                if (visited.contains(block)) continue;
                visited.add(block);
                for (Node node = block.next; Objects.nonNull(node) && Objects.nonNull(node.next); node = node.next) {
                    if (node instanceof Branch){
                        queue.offer(((Branch) node).thenBlock);
                        queue.offer(((Branch) node).elseBlock);
                    }
                    else if (node instanceof Jump){
                        BasicBlock target = ((Jump) node).target;
                        if (blockRes.containsKey(target) && blockRes.get(target).equals(1)){
                            target.tail.prev.next = null;
                            block.append(target.next);
                            node.remove();
                        }
                        else{
                            queue.offer(target);
                        }
                    }
                }
            }
        }

    }
}
