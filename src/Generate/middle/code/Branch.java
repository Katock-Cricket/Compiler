package Generate.middle.code;

import Generate.middle.operand.Operand;

public class Branch extends Node{
    public Operand cond;
    public final BasicBlock thenBlock, elseBlock;
    private final String op = "Branch";

    public Branch(Operand cond, BasicBlock thenBlock, BasicBlock elseBlock){
        this.cond=cond;
        this.thenBlock=thenBlock;
        this.elseBlock=elseBlock;
    }

    @Override
    public String toString() {
        return op + " " + cond + " ? " + thenBlock + " : " + elseBlock;
    }
}
