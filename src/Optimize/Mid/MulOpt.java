package Optimize.Mid;

import Generate.middle.code.*;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;

import java.util.Queue;

import static Generate.middle.code.Node.insertAfter;
import static Generate.middle.code.Node.insertBefore;
import static Optimize.Mid.MathUtil.isLog2;
import static Optimize.Mid.MathUtil.log2;

public class MulOpt implements MidOpt{
    public MulOpt(){
    }

    private void mulOpt(Node node, Operand src1, Operand src2, Symbol dst){
        if (src1 instanceof Immediate && src2 instanceof Immediate ||
                src1 instanceof Symbol && src2 instanceof Symbol)
            return;
        if (src1 instanceof Immediate && src2 instanceof Symbol){
            Operand tmp = src2;
            src2 = src1;
            src1 = tmp;
        }
        if (src1 instanceof Symbol && src2 instanceof Immediate){
            int val = ((Immediate) src2).value;
            if (isLog2(val)){
                if (val == 0){
                    UnaryInstr move = new UnaryInstr(UnaryOp.MOVE, new Immediate(0), dst);
                    insertAfter(node, move);
                }
                else {
                    val = log2(val);
                    BinaryInstr sll = new BinaryInstr(BinaryOp.SLL, src1, new Immediate(val), dst);
                    insertAfter(node, sll);
                }
                node.remove();
            }
            else if (val<8){
                Symbol tmp = Symbol.temp(BasicType.INT, RefType.ITEM);
                insertBefore(new UnaryInstr(UnaryOp.MOVE, new Immediate(0), tmp), node);
                for(int i=0;i<val;i++){
                    insertBefore( new BinaryInstr(BinaryOp.ADD, src1, tmp, tmp), node);
                }
                insertBefore(new UnaryInstr(UnaryOp.MOVE, tmp, dst), node);
                if (val<0){
                    insertBefore(new UnaryInstr(UnaryOp.NEG, dst, dst), node);
                }
                node.remove();
            }
        }
    }

    @Override
    public void method(Node node, Queue<BasicBlock> queue) {
        detectBranch(node, queue);
        if (! (node instanceof BinaryInstr)) return;
        BinaryInstr binary = (BinaryInstr) node;
        BinaryOp op = binary.op;
        Operand src1 = binary.src1, src2 = binary.src2;
        Symbol dst = binary.dst;
        if (op.equals(BinaryOp.MUL))
            mulOpt(node, src1, src2, dst);
    }
}
