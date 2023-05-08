package Optimize.Mid;

import Generate.middle.code.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public interface MidOpt {

    void method(Node node, Queue<BasicBlock> queue);

    default void optimize(MiddleCode middleCode){
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
    }

    default void detectBranch(Node node, Queue<BasicBlock> queue){
        if (node instanceof Branch){
            queue.offer(((Branch) node).thenBlock);
            queue.offer((((Branch) node).elseBlock));
        }else if (node instanceof Jump)
            queue.offer((((Jump) node).target));
    }

}
