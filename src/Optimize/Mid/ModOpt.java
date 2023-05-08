package Optimize.Mid;

import Generate.middle.code.*;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Symbol;

import java.util.Queue;

public class ModOpt implements MidOpt{
    public ModOpt(){}

    /**
     * mod src1 src2 dst1 replace by:
     * div src1 src2 dst2
     * mul src2 dst2 dst3
     * sub src1 dst3 dst1
     */
    @Override
    public void method(Node node, Queue<BasicBlock> queue) {
        detectBranch(node, queue);
        if (!(node instanceof BinaryInstr)) return;
        BinaryInstr binary = (BinaryInstr) node;
        if (binary.op.equals(BinaryOp.MOD) &&
            binary.src1 instanceof Symbol &&
            binary.src2 instanceof Immediate){
            Symbol src1 = (Symbol) binary.src1;
            Immediate src2 = (Immediate) binary.src2;
            Symbol dst1 = binary.dst,
                    dst2 = Symbol.temp(BasicType.INT, RefType.ITEM),
                    dst3 = Symbol.temp(BasicType.INT, RefType.ITEM);
            BinaryInstr div = new BinaryInstr(BinaryOp.DIV, src1, src2, dst2),
                    mul = new BinaryInstr(BinaryOp.MUL, src2, dst2, dst3),
                    sub = new BinaryInstr(BinaryOp.SUB, src1, dst3, dst1);
            node.insertNodesBefore(div, mul, sub);
            node.remove();
        }
    }
}
