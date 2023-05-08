package Generate.middle.code;

import java.util.Objects;

public abstract class Node {
    public Node prev, next;

    public Node(){}

    public void remove(){
        if (Objects.nonNull(prev))
            prev.next=next;
        if (Objects.nonNull(next))
            next.prev=prev;
    }

    public static void insertAfter(Node node, Node nodeNew){
        nodeNew.prev = node;
        nodeNew.next = node.next;
        if (Objects.nonNull(node.next))
            node.next.prev = nodeNew;
        node.next = nodeNew;
    }

    public static void insertBefore(Node nodeNew, Node node){
        nodeNew.next = node;
        nodeNew.prev = node.prev;
        if (Objects.nonNull(node.prev))
            node.prev.next = nodeNew;
        node.prev = nodeNew;
    }

    public void insertNodesBefore(Node... nodes){
        for (Node node : nodes) {
            insertBefore(node, this);
        }
    }
}
