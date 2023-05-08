package Generate.middle.code;

import java.util.Objects;

public class BasicBlock extends Node{
    public final String label;
    public final Node tail;
    public final BlockType type;

    public BasicBlock(String label, BlockType type){
        this.label=label;
        this.type=type;
        tail=new Node(){};
        next=tail;
        tail.prev=this;
    }

    public void append(Node node){
        Node last = tail.prev;
        last.next = node;
        node.prev = last;
        Node tail = node;
        while (Objects.nonNull(tail.next))
            tail = tail.next;
        tail.next = this.tail;
        this.tail.prev = tail;
    }

    @Override
    public String toString() {
        return label;
    }
}
