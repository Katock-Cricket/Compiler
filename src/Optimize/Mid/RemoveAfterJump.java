package Optimize.Mid;

import Generate.middle.code.*;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import static Generate.middle.code.FuncCall.intFuncCall;
import static Generate.middle.code.FuncCall.voidFuncCall;
import static Generate.middle.code.Node.insertAfter;
import static Generate.middle.code.Node.insertBefore;
import static Generate.middle.code.Return.IntReturn;

public class RemoveAfterJump implements MidOpt {
    private boolean removedByPrev = false;
    public RemoveAfterJump() {
    }
    /**
     * remove useless instr after Branch&Jump in a BasicBlock
     * remove useless MOVE, eg:
     *         ...dst1
     *         mov dst1 dst2 (dst1 is tmp, so that can be removed)
     *         ...
     *         mov src1 dst1
     *         ... src2 src3 ...(src2/src3 == src1 is tmp)
     * then remove mov, let ...dst2
     */
    private void removeMoveByPrev(UnaryInstr move){
        Symbol src = (Symbol) move.src, dst = move.dst;
        move.remove();
        for (Node prev = move.prev; Objects.nonNull(prev) && Objects.nonNull(prev.prev); prev = prev.prev){
            Node newPrev;
            if (prev instanceof Input){
                if (!((Input) prev).dst.equals(src))
                    continue;
                newPrev = new Input(dst);
            }
            else if (prev instanceof UnaryInstr){
                UnaryInstr unary = (UnaryInstr) prev;
                if (!(unary).dst.equals(src))
                    continue;
                newPrev = new UnaryInstr(unary.op, unary.src, dst);
            }
            else if (prev instanceof BinaryInstr){
                BinaryInstr binary = (BinaryInstr) prev;
                if (!binary.dst.equals(src))
                    continue;
                newPrev = new BinaryInstr(binary.op, binary.src1, binary.src2, dst);
            }
            else if (prev instanceof Offset){
                Offset offset = (Offset) prev;
                if (!offset.target.equals(src))
                    continue;
                newPrev = new Offset(offset.base, offset.offset, dst);
            }
            else if (prev instanceof LoadInstr){
                LoadInstr load = (LoadInstr) prev;
                if (!load.dst.equals(src))
                    continue;
                newPrev = new LoadInstr(dst, load.addr);
            }
            else if (prev instanceof FuncCall && Objects.nonNull(((FuncCall) prev).ret)){
                FuncCall call = (FuncCall) prev;
                if (!call.ret.equals(src)) continue;
                newPrev = intFuncCall(call.func, call.params, dst);
            }
            else {
                continue;
            }
            insertBefore(newPrev, prev);
            prev.remove();
            removedByPrev = true;
        }
    }

    private void removeMoveByNext(UnaryInstr move){
        Operand src = move.src;
        Symbol dst = move.dst;
        Operand toReplace = removedByPrev? dst : src;
        move.remove();
        for (Node next = move.next; Objects.nonNull(next) && Objects.nonNull(next.next) && Objects.nonNull(next.next.next); next = next.next){
            Node newNext;
            if (next instanceof PrintInt){
                if (!((PrintInt) next).value.equals(dst))
                    continue;
                newNext = new PrintInt(toReplace);
            }
            else if (next instanceof UnaryInstr){
                UnaryInstr unary = (UnaryInstr) next;
                if (!(unary).src.equals(dst))
                    continue;
                newNext = new UnaryInstr(unary.op, toReplace, unary.dst);
            }
            else if (next instanceof BinaryInstr){
                BinaryInstr binary = (BinaryInstr) next;
                if (binary.src1.equals(dst) && !(binary.src2.equals(dst)))
                    newNext = new BinaryInstr(binary.op, toReplace, binary.src2, binary.dst);
                else if (binary.src2.equals(dst) && !(binary.src1.equals(dst)))
                    newNext = new BinaryInstr(binary.op, binary.src1, toReplace, binary.dst);
                else if (binary.src1.equals(dst) && binary.src2.equals(dst)){
                    newNext = new BinaryInstr(binary.op, toReplace, toReplace, binary.dst);
                }
                else
                    continue;
            }
            else if (next instanceof Offset){
                Offset offset = (Offset) next;
                if (!(offset.offset.equals(dst)))
                    continue;
                newNext = new Offset(offset.base, toReplace, offset.target);
            }
            else if (next instanceof StoreInstr){
                StoreInstr load = (StoreInstr) next;
                if (!load.src.equals(dst))
                    continue;
                newNext = new StoreInstr(toReplace, load.addr);
            }
            else if (next instanceof FuncCall){
                FuncCall call = (FuncCall) next;
                FuncScope newFunc = call.func;
                List<Operand> newParams = new ArrayList<>();
                for(Operand param : call.params){
                    if (param.equals(dst))
                        newParams.add(toReplace);
                    else
                        newParams.add(param);
                }
                if (call.type.equals(ReturnType.VOID))
                    newNext = voidFuncCall(newFunc, newParams);
                else
                    newNext = intFuncCall(newFunc ,newParams, call.ret);
            }
            else if (next instanceof Branch){
                Branch branch = (Branch) next;
                Operand cond = branch.cond;
                if (!(cond.equals(dst)))
                    continue;
                newNext = new Branch(toReplace, branch.thenBlock, branch.elseBlock);
            }
            else if (next instanceof Return && ((Return) next).type.equals(ReturnType.INT)){
                newNext = IntReturn(toReplace);
            }
            else {
                continue;
            }
            insertAfter(next, newNext);
            next.remove();
        }
    }

    @Override
    public void method(Node node, Queue<BasicBlock> queue) {
        if (node instanceof Branch){
            queue.offer(((Branch) node).thenBlock);
            queue.offer((((Branch) node).elseBlock));
            while (Objects.nonNull(node.next.next))
                node.next.remove();
        }else if (node instanceof Jump) {
            queue.offer((((Jump) node).target));
            while (Objects.nonNull(node.next.next))
                node.next.remove();
        }else if(node instanceof Return) {
            while (Objects.nonNull(node.next.next))
                node.next.remove();
        }

//        if (!(node instanceof UnaryInstr && ((UnaryInstr) node).op.equals(UnaryOp.MOVE)))
//            return;
//        UnaryInstr move = (UnaryInstr) node;
//        removedByPrev = false;
//        if (move.src instanceof Symbol && ((Symbol) move.src).offset ==-1 ) {
//            removeMoveByPrev(move); //prev mov -> prev
//        }
//        if (move.dst.offset ==-1){
//            removeMoveByNext(move); //mov next -> next
//        }
    }
}