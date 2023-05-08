package Optimize.Mid;

import Generate.middle.code.*;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;

import java.util.Queue;

import static Generate.middle.code.Node.insertAfter;
import static Optimize.Mid.MathUtil.*;

public class DivOpt implements MidOpt {
    public DivOpt(){}

    private void divOpt(Node node, Operand src1, Operand src2, Symbol dst){
        if (src1 instanceof Symbol && src2 instanceof Immediate){
            final int negDivisor = 128, addMarker = 64, s32ShiftMask = 31;
            int magic, more;
            int divisor = ((Immediate) src2).value,
                    abs = Math.abs(divisor),
                    log2d = 31 - clz(abs);
            if ((abs&(abs-1)) == 0){
                magic = 0;
                more = (divisor < 0? (log2d | negDivisor) : log2d) & 0xFF;
            }
            else {
                int rem, proposed;
                int [] divResult = divideU64To32(1<<(log2d-1), 0, abs);
                rem = divResult[1];
                proposed = divResult[0];
                proposed += proposed;
                int twiceRem = rem + rem;
                if (getUnsignedInt(twiceRem) >= getUnsignedInt(abs) ||
                    getUnsignedInt(twiceRem) < getUnsignedInt(rem))
                    proposed ++;
                more = (log2d | addMarker) & 0xFF;
                proposed ++;
                magic = proposed;
                if (divisor < 0)
                    more |= negDivisor;
            }
            int shift = more & s32ShiftMask;
            int mask = (1<<shift);
            int sign = ((more&(1<<7))!=0)? -1 : 0;
            int isPow2 = (magic==0)? 1 : 0;
            Symbol q = Symbol.temp(BasicType.INT, RefType.ITEM);
            Symbol qSign = Symbol.temp(BasicType.INT, RefType.ITEM);
            int andRight = mask - isPow2;
            Symbol qAnd = Symbol.temp(BasicType.INT, RefType.ITEM);
            node.insertNodesBefore(
                    new BinaryInstr(BinaryOp.MULHI, new Immediate(magic), src1, q),
                    new BinaryInstr(BinaryOp.ADD, q, src1, q),
                    new BinaryInstr(BinaryOp.LT, q, new Immediate(0), qSign),
                    new UnaryInstr(UnaryOp.NEG, qSign, qSign),
                    new BinaryInstr(BinaryOp.AND, qSign, new Immediate(andRight), qAnd),
                    new BinaryInstr(BinaryOp.ADD, q, qAnd, q),
                    new BinaryInstr(BinaryOp.SRA, q, new Immediate(shift), q),
                    new BinaryInstr(BinaryOp.XOR, q, new Immediate(sign), q),
                    new BinaryInstr(BinaryOp.SUB, q, new Immediate(sign), q),
                    new UnaryInstr(UnaryOp.MOVE, q, dst)
            );
            node.remove();
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
        if (op.equals(BinaryOp.DIV))
            divOpt(node, src1, src2, dst);
    }
}
