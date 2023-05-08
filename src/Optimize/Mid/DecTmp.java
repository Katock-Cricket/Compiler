package Optimize.Mid;

import Generate.middle.code.*;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

public class DecTmp implements MidOpt{

    void replaceTmp(Node node, Operand tmp1, Symbol tmp2){
        if (node instanceof Input){
            if(((Input) node).dst.equals(tmp1))
                ((Input) node).dst = tmp2;
        } else if (node instanceof PrintInt){
            if (((PrintInt) node).value.equals(tmp1))
                ((PrintInt) node).value = tmp2;
        } else if (node instanceof UnaryInstr){
            if (((UnaryInstr) node).src.equals(tmp1))
                ((UnaryInstr) node).src = tmp2;
            if (((UnaryInstr) node).dst.equals(tmp1))
                ((UnaryInstr) node).dst = tmp2;
        } else if (node instanceof BinaryInstr){
            if (((BinaryInstr) node).src1.equals(tmp1))
                ((BinaryInstr) node).src1 = tmp2;
            if (((BinaryInstr) node).src2.equals(tmp1))
                ((BinaryInstr) node).src2 = tmp2;
            if (((BinaryInstr) node).dst.equals(tmp1))
                ((BinaryInstr) node).dst = tmp2;
        } else if (node instanceof Offset){
            if (((Offset) node).base.equals(tmp1))
                ((Offset) node).base = tmp2;
            if (((Offset) node).target.equals(tmp1))
                ((Offset) node).target = tmp2;
        } else if (node instanceof Return){
            if (((Return) node).value.equals(tmp1))
                ((Return) node).value = tmp2;
        } else if (node instanceof LoadInstr){
            if (((LoadInstr) node).dst.equals(tmp1))
                ((LoadInstr) node).dst = tmp2;
        } else if (node instanceof StoreInstr){
            if (((StoreInstr) node).src.equals(tmp1))
                ((StoreInstr) node).src = tmp2;
        } else if (node instanceof FuncCall){
            LinkedList<Operand> newParams = new LinkedList<>();
            ((FuncCall) node).params.forEach(param -> {
                if (param.equals(tmp1))
                    param = tmp2;
                newParams.add(param);
            });
            ((FuncCall) node).params = newParams;
        } else if (node instanceof Branch){
            if (((Branch) node).cond.equals(tmp1))
                ((Branch) node).cond = tmp2;
        }
    }

    @Override
    public void method(Node node, Queue<BasicBlock> queue) {
        detectBranch(node, queue);
        if (!(node instanceof UnaryInstr)) return;
        if(!((UnaryInstr) node).op.equals(UnaryOp.MOVE)) return;
        UnaryInstr move = (UnaryInstr) node;
        Operand src = move.src;
        if (! (src instanceof Symbol)) return;
        if (((Symbol) src).offset != -1) return;
        Symbol dst = move.dst;
        if (dst.offset != -1) return;
        //now this instr is mov tmp1 tmp2
        //then replace all tmp1 with tmp2 before instr
        for (Node prev = move.prev; Objects.nonNull(prev) && Objects.nonNull(prev.prev); prev = prev.prev)
            replaceTmp(prev, src, dst);
        move.remove();
    }
}
