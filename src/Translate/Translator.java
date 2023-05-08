package Translate;

import Generate.middle.code.MiddleCode;
import Generate.middle.code.*;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Operand;
import Generate.middle.code.FuncScope;
import Generate.middle.code.RefType;
import Generate.middle.operand.Symbol;
import Translate.Hardware.Reg;
import Translate.Instruction.Arith.*;
import Translate.Instruction.BranchJump.BNE;
import Translate.Instruction.BranchJump.J;
import Translate.Instruction.BranchJump.JAL;
import Translate.Instruction.BranchJump.JR;
import Translate.Instruction.Exception.SYSCALL;
import Translate.Instruction.Instruction;
import Translate.Instruction.LoadStore.LW;
import Translate.Instruction.LoadStore.SW;
import Translate.Instruction.Logic.*;
import Translate.Instruction.Move.*;
import Translate.Instruction.Shift.SLL;
import Translate.Instruction.Shift.SLLV;
import Translate.Instruction.Shift.SRA;
import Translate.Instruction.Shift.SRAV;

import java.util.*;

import static Generate.middle.operand.Symbol.sizeOfInt;
import static Translate.Hardware.Reg.*;

public class Translator {
    private final MiddleCode middleCode;
    private final RegManager regManager;
    private final Mips mips;
    private FuncScope curFunc;
    private int curStackSize;
    private final HashSet<BasicBlock> visited;
    private final Stack<BasicBlock> BlockQueue;
    private final HashMap<Symbol, Integer> useCnt;

    public Translator(MiddleCode middleCode){
        this.middleCode=middleCode;
        regManager=new RegManager();
        mips=new Mips();
        curFunc=null;
        curStackSize=0;
        visited=new HashSet<>();
        BlockQueue = new Stack<>();
        useCnt=new HashMap<>();
    }

    private void writeBackReg(Reg reg, Symbol symbol){
        if (symbol.offset<0){
            curStackSize += sizeOfInt;
            symbol.offset=curStackSize;
            symbol.isLocal=true;
        }
        if (symbol.isLocal)
            mips.append(new SW(reg, -symbol.offset, sp));
        else
            mips.append(new SW(reg, symbol.offset, gp));
    }

    private void clearReg(boolean writeBack){
        if (writeBack){
            Map<Reg,Symbol> allocatedReg = regManager.allocatedRegs;
            for(Map.Entry<Reg,Symbol>entry: allocatedReg.entrySet())
                writeBackReg(entry.getKey(), entry.getValue());
        }
        regManager.clear();
    }

    private void decTmpUse(Symbol symbol){
        if (symbol.offset>=0) // not tmp
            return;
        if (!useCnt.containsKey(symbol)) // not tmp
            throw new AssertionError("Wrong cnt of tmp");
        int cnt = useCnt.get(symbol);
        if (cnt == 1) {
            useCnt.remove(symbol); // never use
            if (regManager.isAllocated(symbol)) // free regUse
                regManager.free(regManager.getReg(symbol));
        }
        else
            useCnt.put(symbol, cnt-1);
    }

    private void addTmpUse(Operand operand){
        if (!(operand instanceof Symbol))
            return;
        Symbol symbol= ((Symbol) operand);
        if (symbol.offset<0)
            useCnt.merge(symbol, 1, Integer::sum);
    }

    private void recordTmpUse(BasicBlock block){
        useCnt.clear();
        for (Node node = block.next; Objects.nonNull(node.next); node=node.next){
            if (node instanceof BinaryInstr){
                addTmpUse(((BinaryInstr) node).src1);
                addTmpUse(((BinaryInstr) node).src2);
            }
            else if (node instanceof UnaryInstr) {
                addTmpUse(((UnaryInstr) node).src);
            }
            else if (node instanceof Input){
                if (((Input) node).dst.refType.equals(RefType.POINTER))
                    addTmpUse(((Input) node).dst);
            }
            else if (node instanceof PrintInt){
                addTmpUse(((PrintInt) node).value);
            }
            else if (node instanceof FuncCall){
                for (Operand o : ((FuncCall) node).params)
                    addTmpUse(o);
            }
            else if (node instanceof Return){
                Return retInstr = (Return) node;
                if (Objects.nonNull(retInstr.value))
                    addTmpUse(retInstr.value);
            }
            else if (node instanceof Offset){
                addTmpUse(((Offset) node).base);
                addTmpUse(((Offset) node).offset);
            }
            else if (node instanceof LoadInstr){
                addTmpUse(((LoadInstr) node).addr);
            }
            else if (node instanceof StoreInstr){
                addTmpUse(((StoreInstr) node).src);
                addTmpUse(((StoreInstr) node).addr);
            }
            else if (node instanceof Branch){
                addTmpUse(((Branch) node).cond);
            }
        }
    }

    private Reg allocReg(Symbol symbol, boolean isSrc){
        // already alloc
        if (regManager.isAllocated(symbol)){
            regManager.updateCache(symbol);
            return regManager.getReg(symbol);
        }
        // no more regs, find one to write back
        if (regManager.freeRegs.isEmpty()){
            Reg reg = regManager.regToReplace();
            Symbol symToReplace = regManager.free(reg);
            writeBackReg(reg, symToReplace);
        }
        Reg reg = regManager.allocReg(symbol);
        if (isSrc && symbol.offset>=0){
            if (symbol.isLocal)
                mips.append(new LW(reg, -symbol.offset, sp));
            else
                mips.append(new LW(reg, symbol.offset, gp));
        }
        return reg;
    }

    private void translateBinaryInstr(BinaryInstr binaryInstr){ // dst = src1 op src2
        Operand src1= binaryInstr.src1, src2= binaryInstr.src2;
        Symbol dst = binaryInstr.dst;
        BinaryOp op = binaryInstr.op;
        Reg regDst, regSrc1, regSrc2;
        // imm imm || imm sym || sym imm || sym sym
        if (src1 instanceof Immediate && src2 instanceof Immediate){ // use li
            int result = MathUtil.calImmOpImm(((Immediate) src1).value, op, ((Immediate) src2).value);
            if (op.equals(BinaryOp.MOVN) && ((Immediate) src2).value==0 || op.equals(BinaryOp.MOVZ) && ((Immediate) src2).value!=0)
                return;
            regDst = allocReg(dst, false);
            mips.append(new LI(regDst, result));
        }
        else if (src1 instanceof Symbol && src2 instanceof Immediate){ // use I type instr
            regSrc1 = allocReg((Symbol) src1, true);
            decTmpUse((Symbol) src1);
            regDst = allocReg(dst, false);
            int imm = ((Immediate) src2).value;
            if (Short.MIN_VALUE <= imm && Short.MAX_VALUE >= imm) // imm is 16bit, can't be more than that
                mips.append(TranslateUtil.translateI(op, regDst, regSrc1, imm));
            else{ // else use reg to store imm
                mips.append(new LI(v1, imm));
                mips.append(TranslateUtil.translateR(op ,regDst, regSrc1, v1));
            }
        }
        else if (src1 instanceof Symbol && src2 instanceof Symbol){ // use R type instr
            regSrc1 = allocReg((Symbol) src1, true);
            regSrc2 = allocReg((Symbol) src2, true);
            decTmpUse((Symbol) src1);
            decTmpUse((Symbol) src2);//must dec after alloc, because src have two, is conflict
            regDst = allocReg(dst, false);
            mips.append(TranslateUtil.translateR(op, regDst, regSrc1, regSrc2));
        }
        else if (src1 instanceof Immediate && src2 instanceof Symbol){ // use R type instr
            mips.append(new LI(v1,((Immediate) src1).value));
            regSrc2 = allocReg((Symbol) src2, true);
            decTmpUse((Symbol) src2);
            regDst = allocReg(dst, false);
            mips.append(TranslateUtil.translateR(op, regDst, v1, regSrc2));
        }
        else throw new AssertionError("No possible to reach here");
    }

    private void translateUnaryInstr(UnaryInstr unaryInstr){ // dst = op src(sym or imm)
        Operand src = unaryInstr.src;
        Symbol dst = unaryInstr.dst;
        if (src instanceof Symbol){
            Reg regSrc = allocReg((Symbol) src, true);
            Reg regDst = allocReg(dst, false);
            decTmpUse((Symbol) src);
            switch (unaryInstr.op){
                case ABS: mips.append(new ABS(regDst,regSrc));break;
                case CLO: mips.append(new CLO(regDst,regSrc));break;
                case CLZ: mips.append(new CLZ(regDst, regSrc));break;
                case MOVE: mips.append(new MOVE(regDst, regSrc));break;
                case NEG: mips.append(new SUBU(regDst, zero, regSrc));break;
                case NOT: mips.append(new SEQ(regDst, zero, regSrc));break;
                default: throw new AssertionError("Wrong Unary Op");
            }
        }
        else {
            int imm = ((Immediate) src).value;
            Reg regDst = allocReg(dst, false);
            switch (unaryInstr.op){
                case ABS: mips.append(new LI(regDst, Math.abs(imm)));break;
                case CLO: mips.append(new LI(regDst, MathUtil.clo(imm)));break;
                case CLZ: mips.append(new LI(regDst, MathUtil.clz(imm)));break;
                case MOVE: mips.append(new LI(regDst, imm));break;
                case NEG: mips.append(new LI(regDst, -imm));break;
                case NOT: mips.append(new LI(regDst, imm!=0?0:1));break;
                default: throw new AssertionError("Wrong Unary Op");
            }
        }
    }

    private void translateInput(Input input){
        mips.append(new LI(v0, SYSCALL.READ_INT), new SYSCALL());
        Symbol dst = input.dst;
        Reg regDst = allocReg(dst, false);
        if (dst.refType.equals(RefType.POINTER)){ // array, store in stack
            mips.append(new SW(v0,0,regDst));
        }
        else {
            mips.append(new MOVE(regDst, v0));
        }
    }

    private void translatePrintInt(PrintInt printInt){
        mips.append(new LI(v0, SYSCALL.PRINT_INT));
        if (printInt.value instanceof Immediate)
            mips.append(new LI(a0, ((Immediate) printInt.value).value));
        else {
            Reg regSrc = allocReg((Symbol) printInt.value, true);
            decTmpUse((Symbol) printInt.value);
            mips.append(new MOVE(a0, regSrc));
        }
        mips.append(new SYSCALL());
    }

    private void translatePrintStr(PrintStr printStr){
        mips.append(new LI(a0, Mips.STRING_START_ADDR+mips.constStrAddr.get(printStr.label)),
                new LI(v0, SYSCALL.PRINT_STR), new SYSCALL());
    }

    private void translateReturn(Return returnInstr){
        //write back regs, assign to v0, generate jr $ra
        if (curFunc.isMain){
            clearReg(false);
            mips.append(new LI(v0, SYSCALL.TERMINATE), new SYSCALL());
            return;
        }
        else if (Objects.nonNull(returnInstr.value)){
            Operand retVal = returnInstr.value;
            if (retVal instanceof Immediate){ // return imm;
                mips.append(new LI(v0, ((Immediate) retVal).value));
            }
            else { // return ident;
                Reg regSrc = allocReg((Symbol) retVal, true);
                decTmpUse((Symbol) retVal);
                mips.append(new MOVE(v0, regSrc));
            }
        }
        clearReg(true);
        mips.append(new JR(ra));
    }

    private void translateOffset(Offset offsetInstr){
        Symbol base = offsetInstr.base, target = offsetInstr.target;
        Operand offset = offsetInstr.offset;
        Reg regDst = allocReg(target, true);
        if (base.refType.equals(RefType.POINTER)){ // base is pointer
            if (offset instanceof Symbol){ // target = base + sym
                Reg regSrc1 = allocReg(base, true);
                decTmpUse(base);//not here
                Reg regSrc2 = allocReg((Symbol) offset, true);
                decTmpUse((Symbol) offset);//not here
                mips.append(new ADDU(regDst,regSrc1,regSrc2));
            }
            else { // target = base + imm
                int imm = ((Immediate)offset).value;
                Reg regSrc = allocReg(base, true);
                decTmpUse(base); //not here
                mips.append(new ADDIU(regDst,regSrc,imm));
            }
        }
        else { // base is array
            if (offset instanceof Symbol){ // target = base(stackPtr + base.offset) + sym
                Reg regSrc = allocReg((Symbol) offset, true);
                decTmpUse((Symbol) offset); //not here
                if (base.isLocal)
                    mips.append(new ADDIU(regDst, sp,-base.offset));
                else
                    mips.append(new ADDIU(regDst, gp, base.offset));
                mips.append(new ADDU(regDst,regSrc,regDst));
            }
            else { // target = base(stackPtr + base.offset) + imm = stackPtr + (base.offset+imm)
                if (base.isLocal)
                    mips.append(new ADDIU(regDst, sp, -base.offset + ((Immediate) offset).value));
                else
                    mips.append(new ADDIU(regDst, gp,base.offset + ((Immediate)offset).value));
            }
        }
    }

    private void translateLoadInstr(LoadInstr loadInstr){
        Symbol addr = loadInstr.addr;
        Symbol dst = loadInstr.dst;
        Reg regAddr = allocReg(addr, true);
        decTmpUse(addr);//not here
        Reg regDst = allocReg(dst, false);
        mips.append(new LW(regDst, 0, regAddr));
    }

    private void translateStoreInstr(StoreInstr storeInstr){
        Symbol addr = storeInstr.addr;
        Operand src = storeInstr.src;
        Reg regAddr = allocReg(addr, true);
        if (src instanceof Symbol){
            Reg regSrc = allocReg((Symbol) src, true);
            decTmpUse((Symbol) src);//not here
            decTmpUse(addr);
            mips.append(new SW(regSrc, 0, regAddr));
        }
        else{
            decTmpUse(addr);
            mips.append(new LI(v0, ((Immediate) src).value), new SW(v0, 0, regAddr));
        }

    }

    private void translateJump(Jump jump){
        clearReg(true);
        mips.append(new J(jump.target.label));
        BlockQueue.push(jump.target);
    }

    private void translateFuncCall(FuncCall funcCall){
        clearReg(true);
        // ra in stack
        mips.append(new SW(ra, 0, sp), new ADDIU(a0, sp, -curStackSize-sizeOfInt));
        int offset = 0;
        for (Operand operand : funcCall.params) {
            offset += sizeOfInt;
            if (operand instanceof Immediate) {
                //imm in stack
                mips.append(new LI(v0, ((Immediate) operand).value), new SW(v0, -offset, a0));
            } else {
                //get reg of symbol(alloc if not allocated), put in stack
                Symbol symbol = (Symbol) operand;
                Reg reg = allocReg(symbol, true);
                decTmpUse(symbol);//there is no conflict with alloc
                mips.append(new SW(reg, -offset, a0));
            }
        }
        //update sp and jump
        mips.append(new MOVE(sp, a0));
        clearReg(false);//already wrote back
        mips.append(new JAL(funcCall.func.label));
        //restore reg
        mips.append(new ADDIU(sp, sp, curStackSize+sizeOfInt), new LW(ra, 0, sp));
        //get retVal
        if (Objects.nonNull(funcCall.ret)){
            Reg reg = allocReg(funcCall.ret, false);
            mips.append(new MOVE(reg, v0));
        }
    }

    private void translateBranch(Branch branch){
        clearReg(true);
        Operand cond = branch.cond;
        if (cond instanceof Immediate)
            mips.append(new LI(v0, ((Immediate) cond).value));
        else {
            Symbol symbol = (Symbol) cond;
            if (symbol.isLocal)
                mips.append(new LW(v0, -symbol.offset, sp));
            else
                mips.append(new LW(v0, symbol.offset, gp));
        }
        mips.append(new BNE(v0, zero, branch.thenBlock.label));
        BlockQueue.push(branch.elseBlock);
        mips.append(new J(branch.elseBlock.label));
        BlockQueue.push(branch.thenBlock);
    }

    private void translateBasicBlock(BasicBlock block){
        recordTmpUse(block);
        mips.label= block.label;//every first instr in the block has a label
        for(Node node=block.next; Objects.nonNull(node.next);node=node.next){
            mips.description=node.toString();
            if (node instanceof BinaryInstr)
                translateBinaryInstr((BinaryInstr) node);
            else if (node instanceof UnaryInstr)
                translateUnaryInstr((UnaryInstr) node);
            else if (node instanceof Input)
                translateInput((Input) node);
            else if (node instanceof PrintInt)
                translatePrintInt((PrintInt) node);
            else if (node instanceof PrintStr)
                translatePrintStr((PrintStr) node);
            else if (node instanceof Return)
                translateReturn((Return) node);
            else if (node instanceof Offset)
                translateOffset((Offset) node);
            else if (node instanceof LoadInstr)
                translateLoadInstr((LoadInstr) node);
            else if (node instanceof StoreInstr)
                translateStoreInstr((StoreInstr) node);
            else if (node instanceof Jump)
                translateJump((Jump) node);
            else if (node instanceof FuncCall)
                translateFuncCall((FuncCall) node);
            else if (node instanceof Branch)
                translateBranch((Branch) node);
            else
                throw new AssertionError("Wrong Middle Code Type");
        }
    }

    private void translateFunc(FuncScope func){
        curFunc = func;
        curStackSize = func.stackTop;
        BlockQueue.push(func.body);
        while(!BlockQueue.isEmpty()){
            BasicBlock basicBlock = BlockQueue.pop();
            if (visited.contains(basicBlock))
                continue;
            visited.add(basicBlock);
            translateBasicBlock(basicBlock);
        }
    }

    public Mips generateMips(){
        //add const Str
        for(Map.Entry<String,String> entry : middleCode.globalStr.entrySet()){
            mips.addConstStr(entry.getKey(), entry.getValue());
        }
        //add global var
        for (Map.Entry<String,Integer> entry : middleCode.globalVar.entrySet()){
            mips.mem.sw(middleCode.globalAddr.get(entry.getKey()), entry.getValue());
        }
        //add global arr
        for(Map.Entry<String, List<Integer>> entry : middleCode.globalArr.entrySet()){
            int i=0;
            for(Integer val : entry.getValue()){
                mips.mem.sw(middleCode.globalAddr.get(entry.getKey())+sizeOfInt*(i++), val);
            }
        }
        //add main func
        mips.append(new J(middleCode.mainFunc.label));
        //add func
        for(FuncScope func : middleCode.func.values())
            translateFunc(func);
        return mips;
    }

    static class MathUtil {
        private static String to32BitsBinary(int a){
            String b = Integer.toBinaryString(a);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i=0;i<32-b.length();i++)
                stringBuilder.append('0');
            stringBuilder.append(b);
            return stringBuilder.toString();
        }

        static int clo(int a){
            int cnt = 0;
            for(char i : to32BitsBinary(a).toCharArray())
                if (i == '1')
                    cnt++;
                else break;
            return cnt;
        }

        static int clz(int a){
            int cnt = 0;
            for(char i : to32BitsBinary(a).toCharArray())
                if (i == '0')
                    cnt++;
                else break;
            return cnt;
        }

        static int calImmOpImm(int a, BinaryOp op, int b){
            switch (op){
                case ADD: return a+b;
                case SUB: return a-b;
                case ANDL: return (a!=0)&&(b!=0)?1:0;
                case ORL: return (a!=0)||(b!=0)?1:0;
                case AND: return a&b;
                case OR: return a|b;
                case XOR: return a^b;
                case MUL: return a*b;
                case DIV: return a/b;
                case MOD: return a%b;
                case GE: return (a>=b)?1:0;
                case GT: return (a>b)?1:0;
                case LE: return (a<=b)?1:0;
                case LT: return (a<b)?1:0;
                case EQ: return (a==b)?1:0;
                case NE: return (a!=b)?1:0;
                case SLL: return a<<(b&0x1f); // no more than 31 bits
                case SRA: return a>>(b&0x1f);
                case MOVN: case MOVZ: return a;
                case MULHI: return (int) ((long) a * (long) b >> (Integer.BYTES << 3));
                default: throw new AssertionError("Wrong BinaryOp");
            }
        }
    }

    static class TranslateUtil {
        static Instruction[] translateR(BinaryOp op, Reg dst, Reg src1, Reg src2){
            Instruction[] ret = new Instruction[3];
            switch (op){
                case ADD:ret[0] = new ADDU(dst, src1, src2); break;
                case SUB:ret[0] = new SUBU(dst, src1, src2); break;
                case ANDL:
                case AND:ret[0] = new AND(dst, src1, src2); break;
                case ORL:
                case OR: ret[0] = new OR(dst, src1, src2); break;
                case XOR:ret[0] = new XOR(dst, src1, src2); break;
                case GE: ret[0] = new SGE(dst, src1, src2); break;
                case GT: ret[0] = new SGT(dst, src1, src2); break;
                case LE: ret[0] = new SLE(dst, src1, src2); break;
                case LT: ret[0] = new SLT(dst, src1, src2); break;
                case EQ: ret[0] = new SEQ(dst, src1, src2); break;
                case NE: ret[0] = new SNE(dst, src1, src2); break;
                case SLL:ret[0] = new SLLV(dst, src1, src2); break;
                case SRA:ret[0] = new SRAV(dst, src1, src2); break;
                case MOVZ:ret[0] = new MOVZ(dst, src1,src2); break;
                case MOVN:ret[0] = new MOVN(dst, src1,src2); break;
                case MUL:
                    ret[0] = new MULT(src1, src2);
                    ret[1] = new MFLO(dst);
                    break;
                case DIV:
                    ret[0] = new DIV(src1, src2);
                    ret[1] = new MFLO(dst);
                    break;
                case MOD:
                    ret[0] = new DIV(src1, src2);
                    ret[1] = new MFHI(dst);
                    break;
                case MULHI:
                    ret[0] = new MULT(src1, src2);
                    ret[1] = new MFHI(dst);
                    break;
                default: throw new AssertionError("Wrong BinaryOp");
            }
            return ret;
        }

        static Instruction[] translateI(BinaryOp op, Reg dst, Reg src, int imm){
            Instruction[] ret = new Instruction[4];
            switch (op){
                case ADD: ret[0] = new ADDIU(dst, src, imm); break;
                case SUB: ret[0] = new ADDIU(dst, src, -imm);break;
                case ANDL:
                case AND:ret[0] = new ANDI(dst, src, imm); break;
                case ORL:
                case OR: ret[0] = new ORI(dst, src, imm); break;
                case XOR:ret[0] = new XORI(dst, src, imm); break;
                case GE: ret[0] = new SGEI(dst, src, imm); break;
                case GT: ret[0] = new SGTI(dst, src, imm); break;
                case LE: ret[0] = new SLEI(dst, src, imm); break;
                case LT: ret[0] = new SLTI(dst, src, imm); break;
                case EQ: ret[0] = new SEQI(dst, src, imm); break;
                case NE: ret[0] = new SNEI(dst, src, imm); break;
                case SLL:ret[0] = new SLL(dst, src, imm); break;
                case SRA:ret[0] = new SRA(dst, src, imm); break;
                case MOVZ: if (imm==0) ret[0] = new MOVE(dst, src);break;
                case MOVN: if (imm!=0) ret[0] = new MOVE(dst, src);break;
                case MUL: // don't have I, use R
                    ret[0] = new LI(v1, imm);
                    ret[1] = new MULT(src, v1);
                    ret[2] = new MFLO(dst);
                    break;
                case DIV:
                    ret[0] = new LI(v1, imm);
                    ret[1] = new DIV(src, v1);
                    ret[2] = new MFLO(dst);
                    break;
                case MOD:
                    ret[0] = new LI(v1 ,imm);
                    ret[1] = new DIV(src, v1);
                    ret[2] = new MFHI(dst);
                    break;
                case MULHI:
                    ret[0] = new LI(v1, imm);
                    ret[1] = new MULT(src, v1);
                    ret[2] = new MFHI(dst);
                    break;
                default:
                    throw new AssertionError("Wrong BinaryOp");
            }
            return ret;
        }
    }
}

