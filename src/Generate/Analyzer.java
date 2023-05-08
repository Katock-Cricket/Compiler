package Generate;

import Generate.middle.SymTab;
import Generate.middle.code.UnaryOp;
import Generate.middle.code.*;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;
import Parse.CompUnit;
import Parse.Printer;
import Parse.decl.*;
import Parse.expr.multi.*;
import Parse.expr.unary.FuncCall;
import Parse.expr.unary.Number;
import Parse.expr.unary.*;
import Parse.func.ArrayDim;
import Parse.func.FuncDef;
import Parse.func.FuncParam;
import Parse.func.MainFuncDef;
import Parse.stmt.cpl.*;
import Parse.stmt.spl.*;
import Tokenize.token.Ident;
import Tokenize.token.Token;
import Tokenize.token.TokenType;

import java.util.*;

import static Generate.Analyzer.Calculator.calExp;

public class Analyzer {
    SymTab curSymTab;
    int blockCnt, blockDep, stackSize;
    private FuncScope curFunc;
    public final MiddleCode middleCode;
    private BasicBlock curBlock;
    private final Stack<BasicBlock> loopBlocks, loopFollows;

    public Analyzer() {
        curSymTab = new SymTab("GLOBAL", null);
        blockCnt = blockDep = stackSize = 0;
        curFunc = null;
        middleCode = new MiddleCode();
        loopBlocks = new Stack<>();
        loopFollows = new Stack<>();
    }

    private int newBlockCnt() {
        return ++blockCnt;
    }

    public static List<Exp> get1DimVals(ArrayInitVal arrayInitVal) { //every val of array can be a sub array, recur to get 1 dim list
        List<Exp> vals = new LinkedList<>();
        if (Objects.nonNull(arrayInitVal.first)) {
            //get first
            InitVal first = arrayInitVal.first;
            if (first instanceof ExpInitVal)
                vals.add(((ExpInitVal) first).exp);
            else
                vals.addAll(get1DimVals((ArrayInitVal) first));
            // get vals
            Iterator<InitVal> valIt = arrayInitVal.vals.iterator();
            while (valIt.hasNext()) {
                InitVal val = valIt.next();
                if (val instanceof ExpInitVal)
                    vals.add(((ExpInitVal) val).exp);
                else
                    vals.addAll(get1DimVals((ArrayInitVal) val));
            }
        }
        return vals;
    }

    private void putStack(Symbol symbol) {
        stackSize += symbol.capacity();
        symbol.offset = stackSize;
        curFunc.updateStackTop(stackSize);
        symbol.isLocal = true;
    }

    private void putGlobal(Symbol symbol) {
        symbol.offset = curSymTab.capacity;
        if (symbol.refType.equals(RefType.ARRAY))
            middleCode.addGlobalArr(symbol.name, symbol.initialArr, symbol.offset);
        else
            middleCode.addGlobalVar(symbol.name, symbol.initialVal, symbol.offset);
    }

    private void analyseArray(Def def)  {
        Iterator<ArrayDef> arrIt = def.arrayDefs.iterator();
        List<Integer> arrayDims = new LinkedList<>();

        int arrSize = 1;
        while (arrIt.hasNext()) { // read all dims, calc the size
            ArrayDef arrayDef = arrIt.next();
            int val = calExp(arrayDef.constExp, curSymTab);
            arrSize *= val;
            arrayDims.add(val);
        }
        if (def.isInitialized()) { // initialized array
            ArrayInitVal arrayInitVal = (ArrayInitVal) def.initVal;
            List<Exp> initExps = get1DimVals(arrayInitVal);
            if (arrayInitVal.isConst || Objects.isNull(curFunc)) { //global or const array, calc by compiler
                List<Integer> initVals = new LinkedList<>();

                Iterator<Exp> expIt = initExps.iterator();
                while (expIt.hasNext())//calc value of initExps
                    initVals.add(calExp(expIt.next(), curSymTab));

                Symbol symbol;
                if (def.isConst)
                    symbol = Symbol.ConstArray(def.ident.name, arrayDims, initVals);
                else
                    symbol = Symbol.InitializedArray(def.ident.name, arrayDims, initVals);
                if (Objects.nonNull(curFunc)) {// in a func, put into stack
                    putStack(symbol);
                    int offset = 0;
                    Iterator<Integer> initValIt = initVals.iterator();
                    while (initValIt.hasNext()) {
                        Symbol ptr = Symbol.temp(BasicType.INT, RefType.POINTER);
                        curBlock.append(new Offset(symbol, new Immediate(offset * Symbol.sizeOfInt), ptr));
                        curBlock.append(new StoreInstr(new Immediate(initValIt.next()), ptr));
                        offset++;
                    }
                } else// is global, not in stack
                    putGlobal(symbol);
                curSymTab.addSym(symbol);
            }
            else {// in func and not const, put into stack, calc by runtime
                Symbol symbol = Symbol.Array(def.ident.name, arrayDims);
                putStack(symbol);
                curSymTab.addSym(symbol);

                int offset = 0;
                Iterator<Exp> expIt = initExps.iterator();
                while (expIt.hasNext()) {
                    Operand operand = analyseExp(expIt.next());
                    Symbol ptr = Symbol.temp(BasicType.INT, RefType.POINTER);
                    curBlock.append(new Offset(symbol, new Immediate(offset * Symbol.sizeOfInt), ptr));
                    curBlock.append(new StoreInstr(operand, ptr));
                    offset++;
                }
            }
        } else { // not initialized
            Symbol symbol;
            if (Objects.nonNull(curFunc)) {// in a func, put into stack
                symbol = Symbol.Array(def.ident.name, arrayDims);
                putStack(symbol);
            } else { //global, all 0 vals
                List<Integer> zeros = new LinkedList<>();
                for (int i = 0; i < arrSize; i++)
                    zeros.add(0);
                symbol = Symbol.InitializedArray(def.ident.name, arrayDims, zeros);
                putGlobal(symbol);
            }
            curSymTab.addSym(symbol);
        }
    }

    private void analyseVar(Def def)  {
        if (def.isInitialized()) {
            ExpInitVal expInitVal = (ExpInitVal) def.initVal; // RVal of def
            if (expInitVal.isConst) { // is const RVal
                int value = calExp(expInitVal.exp, curSymTab);
                Symbol symbol = Symbol.ConstInt(def.ident.name, value);
                if (Objects.nonNull(curFunc)) { // in func
                    putStack(symbol);
                    curBlock.append(new UnaryInstr(UnaryOp.MOVE, new Immediate(value), symbol));
                } else { // not in func
                    putGlobal(symbol);
                }
                curSymTab.addSym(symbol);
            } else { //is var RVal
                Symbol symbol;
                if (Objects.isNull(curFunc)) {// not in func, calc by compiler
                    symbol = Symbol.InitializedInt(def.ident.name, calExp(expInitVal.exp, curSymTab));
                    putGlobal(symbol);
                } else { // in func, calc by runtime
                    symbol = Symbol.Int(def.ident.name);
                    putStack(symbol);
                    curBlock.append(new UnaryInstr(UnaryOp.MOVE, analyseExp(expInitVal.exp), symbol));
                }
                curSymTab.addSym(symbol);
            }
        } else { // dont have RVal,
            Symbol symbol;
            if (Objects.isNull(curFunc)) { // is global 0 var
                symbol = Symbol.InitializedInt(def.ident.name, 0);
                putGlobal(symbol);
            } else { // in func, no init var
                symbol = Symbol.Int(def.ident.name);
                putStack(symbol);
            }
            curSymTab.addSym(symbol);
        }
    }

    private void analyseDef(Def def)  {
        if (def.arrayDefs.isEmpty()) //is single var
            analyseVar(def);
        else // is array
            analyseArray(def);
    }

    private void analyseDecl(Decl decl)  {
        analyseDef(decl.first);
        Iterator<Def> defIt = decl.defs.iterator();
        while (defIt.hasNext())
            analyseDef(defIt.next());
    }

    private void analyseFuncParams(FuncParam funcParam, FuncScope funcScope)  {
        String name = funcParam.ident.name;
        Symbol param;

        if (Objects.isNull(funcParam.first)) { //not array
            param = Symbol.Int(name);
        } else { // is array param
            List<Integer> dimSize = new LinkedList<>();

            Iterator<ArrayDim> arrayDimIt = funcParam.dims.iterator();
            while (arrayDimIt.hasNext()) {
                ArrayDim arrayDim = arrayDimIt.next();
                dimSize.add(calExp(arrayDim.length, curSymTab));
            }
            param = Symbol.Pointer(name, dimSize);
        }
        funcScope.symTab.addSym(param);
        funcScope.params.add(param);
        funcScope.updateStackTop(funcScope.symTab.capacity);
        param.offset = funcScope.symTab.capacity;
        param.isLocal = true;
    }

    private Operand analyseUnaryExp(UnaryExp unaryExp) {
        BaseUnaryExp baseUnaryExp = unaryExp.baseUnaryExp;
        Operand ret = null;
        // is funcCall
        if (baseUnaryExp instanceof FuncCall) {
            FuncCall funcCall = (FuncCall) baseUnaryExp;
            Ident ident = funcCall.name;
            //analyse params
            List<Operand> params = new LinkedList<>();
            if (Objects.nonNull(funcCall.funcRParams)) {// have params
                FuncRParams funcParams = funcCall.funcRParams;
                params.add(analyseExp(funcParams.first));
                Iterator<Exp> followIt = funcParams.params.iterator();
                while (followIt.hasNext())
                    params.add(analyseExp(followIt.next()));
            }
            boolean err = false;
            FuncScope funcScope = middleCode.func.get(ident.name);
            //get ret sym
            if (funcScope.retType.equals(ReturnType.VOID)) {
                if (!err) {
                    curBlock.append(Generate.middle.code.FuncCall.voidFuncCall(funcScope, params));
                }
                return null;
            } else {
                if (!err) {
                    Symbol retSym = Symbol.temp(BasicType.INT, RefType.ITEM);
                    curBlock.append(Generate.middle.code.FuncCall.intFuncCall(funcScope, params, retSym));
                    ret = retSym;
                } else
                    return new Immediate(0);
            }
        }
        //is primaryExp
        else if (baseUnaryExp instanceof PrimaryExp)
            ret = analyseBasePrimaryExp(((PrimaryExp) baseUnaryExp).basePrimaryExp, false);
        //analyse unaryOp
        Iterator<Token> opIt = unaryExp.unaryOp.ops.iterator();
        while (opIt.hasNext()) {
            Symbol tmp = Symbol.temp(BasicType.INT, RefType.ITEM);
            Token tokenOp = opIt.next();
            UnaryInstr unaryInstr = new UnaryInstr(Transfer.TUMap.get(tokenOp.tokenType), ret, tmp);
            curBlock.append(unaryInstr);
            ret = tmp;
        }
        return ret;
    }

    private Operand analyseBUExp(Printer BUExp) {
        if (BUExp instanceof MultiExp)
            return analyseBinaryExp((MultiExp) BUExp);
        if (BUExp instanceof UnaryExp)
            return analyseUnaryExp((UnaryExp) BUExp);
        else
            throw new Error("BinaryExp Exception");
    }

    private Operand analyseBinaryExp(MultiExp multiExp) {
        // get type
        Printer first;
        Iterator<Token> opIt;
        Iterator<?> operandIt;
        if (multiExp instanceof AddExp) {
            first = ((AddExp) multiExp).first;
            opIt = ((AddExp) multiExp).ops.iterator();
            operandIt = ((AddExp) multiExp).operands.iterator();
        } 
        else if (multiExp instanceof EqExp) {
            first = ((EqExp) multiExp).first;
            opIt = ((EqExp) multiExp).ops.iterator();
            operandIt = ((EqExp) multiExp).operands.iterator();
        } 
        else if (multiExp instanceof LAndExp) {
            first = ((LAndExp) multiExp).first;
            opIt = ((LAndExp) multiExp).ops.iterator();
            operandIt = ((LAndExp) multiExp).operands.iterator();
        } 
        else if (multiExp instanceof LOrExp) {
            first = ((LOrExp) multiExp).first;
            opIt = ((LOrExp) multiExp).ops.iterator();
            operandIt = ((LOrExp) multiExp).operands.iterator();
        } 
        else if (multiExp instanceof MulExp) {
            first = ((MulExp) multiExp).first;
            opIt = ((MulExp) multiExp).ops.iterator();
            operandIt = ((MulExp) multiExp).operands.iterator();
        } 
        else if (multiExp instanceof RelExp){
            first = ((RelExp) multiExp).first;
            opIt = ((RelExp) multiExp).ops.iterator();
            operandIt = ((RelExp) multiExp).operands.iterator();
        }
        else
            throw new Error("MultiExp Exception");

        //analyse first
        Operand ret = analyseBUExp(first);
        if (Objects.isNull(ret))
            return null;

        //analyse operands
        while (opIt.hasNext() && operandIt.hasNext()) {
            Token op = opIt.next();
            Printer operand = (Printer) operandIt.next();
            Operand operandRet = analyseBUExp(operand);
            if (Objects.isNull(operandRet))
                return null;
            Symbol tmp = Symbol.temp(BasicType.INT, RefType.ITEM);
            BinaryOp binaryOp = Transfer.TBMap.get(op.tokenType);
            curBlock.append(new BinaryInstr(binaryOp, ret, operandRet, tmp));
            ret = tmp;
        }
        return ret;
    }

    private Operand analyseExp(Exp exp) {
        return analyseBinaryExp(exp.addExp);
    }

    private Operand analyseLAndExp(LAndExp lAndExp) {
        BasicBlock andFollow = new BasicBlock("COND_AND_" + newBlockCnt(), BlockType.BASIC);
        //analyse first
        Operand item = analyseBinaryExp(lAndExp.first);
        if (Objects.isNull(item))
            return null;
        BasicBlock next = new BasicBlock("AND_ITEM_" + newBlockCnt(), BlockType.BASIC);
        curBlock.append(new Branch(item, next, andFollow));
        curBlock = next;
        //analyse operand
        Iterator<EqExp> eqExpIt = lAndExp.operands.iterator();
        while (eqExpIt.hasNext()) {
            item = analyseBinaryExp(eqExpIt.next());
            if (Objects.isNull(item))
                return null;
            next = new BasicBlock("AND_ITEM_" + newBlockCnt(), BlockType.BASIC);
            curBlock.append(new Branch(item, next, andFollow));
            curBlock = next;
        }
        curBlock.append(new Jump(andFollow));
        curBlock = andFollow;
        return item;
    }

    private Operand analyseLOrExp(LOrExp lOrExp) {
        BasicBlock orFollow = new BasicBlock("COND_OR_" + newBlockCnt(), BlockType.BASIC);
        //analyse first
        Operand and = analyseLAndExp(lOrExp.first);
        if (Objects.isNull(and))
            return null;
        BasicBlock next = new BasicBlock("OR_AND_"+newBlockCnt(), BlockType.BASIC);
        curBlock.append(new Branch(and, orFollow, next));
        curBlock = next;
        //analyse operands
        Iterator<LAndExp> lAndExpIt = lOrExp.operands.iterator();
        while (lAndExpIt.hasNext()) {
            and = analyseLAndExp(lAndExpIt.next());
            if (Objects.isNull(and))
                return null;
            next = new BasicBlock("OR_AND_"+newBlockCnt(), BlockType.BASIC);
            curBlock.append(new Branch(and, orFollow, next));
            curBlock = next;
        }
        curBlock.append(new Jump(orFollow));
        curBlock = orFollow;
        return and;
    }

    private Operand analyseCond(Cond cond) {
        return analyseLOrExp(cond.lOrExp);
    }

    private Operand analyseBasePrimaryExp(BasePrimaryExp basePrimaryExp, boolean isLVal) {
        //is subExp
        if (basePrimaryExp instanceof SubExp)
            return analyseExp(((SubExp) basePrimaryExp).exp);
        //is lVal (array or single var
        else if (basePrimaryExp instanceof LVal) {
            LVal lVal = (LVal) basePrimaryExp;
            Symbol lValSym = curSymTab.getSym(lVal.ident.name, true);
            RefType refType = lValSym.refType;

            List<Operand> indexes = new LinkedList<>();//index can be imm or sym
            if (!lVal.indexList.isEmpty() && refType.equals(RefType.ITEM))
                throw new Error("int symbol has index");

            Iterator<Index> indexIt = lVal.indexList.iterator();
            while (indexIt.hasNext())
                indexes.add(analyseExp(indexIt.next().index));
            int indexCnt = indexes.size(), dimCnt = lValSym.dimSize.size();
            //index must be <= dims
            if (indexCnt > dimCnt && refType.equals(RefType.ARRAY) ||
                    indexCnt > dimCnt + 1 && refType.equals(RefType.POINTER))
                throw new Error("indexes more than dim");

            Operand offset = new Immediate(0);
            for (int i = indexCnt - 1; i >= 0; i--) {
                Symbol prod = Symbol.temp(BasicType.INT, RefType.ITEM);
                Operand offsetBase = new Immediate(lValSym.dimBase.get(i));
                curBlock.append(new BinaryInstr(BinaryOp.MUL, indexes.get(i), offsetBase, prod));
                Symbol sum = Symbol.temp(BasicType.INT, RefType.ITEM);
                curBlock.append(new BinaryInstr(BinaryOp.ADD, offset, prod, sum));
                offset = sum;
            }
            if (refType.equals(RefType.ITEM))//is single var
                return lValSym;
            else if (refType.equals(RefType.ARRAY)) {//is array
                Symbol pointer = lValSym.toPointer(1).toPointer(indexCnt);
                curBlock.append(new Offset(lValSym, offset, pointer));
                if (isLVal || indexCnt < lValSym.dimSize.size())
                    return pointer;
                else {
                    Symbol val = Symbol.temp(BasicType.INT, RefType.ITEM);
                    curBlock.append(new LoadInstr(val, pointer));
                    return val;
                }
            } else {//is pointer
                Symbol pointer = lValSym.toPointer(indexCnt);
                curBlock.append(new Offset(lValSym, offset, pointer));
                if (isLVal || indexCnt <= lValSym.dimSize.size())
                    return pointer;
                else {
                    Symbol val = Symbol.temp(BasicType.INT, RefType.ITEM);
                    curBlock.append(new LoadInstr(val, pointer));
                    return val;
                }
            }
        }
        else if (basePrimaryExp instanceof Number)
            return new Immediate(((Number) basePrimaryExp).number.value);
        else
            throw new Error("BasePrimaryExp Exception");
    }

    private Symbol analyseLVal(LVal lVal) {
        Operand lValSym = analyseBasePrimaryExp(lVal, true);
        if (Objects.isNull(lValSym) || !(lValSym instanceof Symbol))
            return null;
        return (Symbol) lValSym;
    }

    private void analyseAssignStmt(AssignStmt assignStmt) {
        Symbol lVal = analyseLVal(assignStmt.lVal);
        Operand rn = analyseExp(assignStmt.exp);
        if (Objects.isNull(rn))
            throw new Error("Assign void to LVal");
        if (Objects.isNull(lVal))
            return;
        if (lVal.refType.equals(RefType.POINTER)){
            curBlock.append(new StoreInstr(rn, lVal));
        }
        else {
            curBlock.append(new UnaryInstr(UnaryOp.MOVE, rn, lVal));
        }
    }

    private void analysePrintStmt(PrintStmt printStmt) {
        Iterator<Exp> expIt = printStmt.params.iterator();
        List<Operand> operands = new LinkedList<>();
        while (expIt.hasNext())
            operands.add(analyseExp(expIt.next()));// analyse all the exps
        String formatString = printStmt.formatString.inner;
        String[] parts = formatString.split("%d", -1);
        for(int i=0; i< parts.length; i++){
            if (!parts[i].isEmpty()){
                String label = middleCode.addGlobalStr(parts[i]);
                curBlock.append(new PrintStr(label));
            }
            if (i < operands.size()){
                curBlock.append(new PrintInt(operands.get(i)));
            }
        }
    }

    private void analyseExpStmt(ExpStmt expStmt) {
        analyseExp(expStmt.exp);
    }

    private void analyseContinueStmt() {
        curBlock.append(new Jump(loopBlocks.peek()));
    }

    private void analyseBreakStmt() {
        curBlock.append(new Jump(loopFollows.peek()));
    }

    private void analyseReturnStmt(ReturnStmt returnStmt) {
        // return must in a func
        if (Objects.isNull(curFunc))
            throw new Error("Return not in func");
        if (curFunc.retType.equals(ReturnType.INT))
            if (Objects.isNull(returnStmt.exp))
                throw new Error("int func should have return val");
            else {
                curBlock.append(Return.IntReturn(analyseExp(returnStmt.exp)));
            }
        else {
            if (Objects.isNull(returnStmt.exp))
                curBlock.append(Return.VoidReturn());
        }
    }

    private void analyseGetIntStmt(GetIntStmt getIntStmt) {
        curBlock.append(new Input(analyseLVal(getIntStmt.lVal)));
    }

    private void analyseIfStmt(IfStmt ifStmt)  {
        Operand cond = analyseCond(ifStmt.cond);
        BasicBlock cur = curBlock;
        BasicBlock follow = new BasicBlock("B_"+newBlockCnt(), BlockType.BASIC);
        BasicBlock then = new BasicBlock("THEN_"+newBlockCnt(), BlockType.BRANCH);
        if (Objects.nonNull(ifStmt._else) && Objects.nonNull(ifStmt.stmt2)) {// have else
            BasicBlock elseBlk = new BasicBlock("ELSE_"+newBlockCnt(), BlockType.BRANCH);
            cur.append(new Branch(cond, then, elseBlk));
            curBlock = then;
            analyseStmt(ifStmt.stmt1);
            curBlock.append(new Jump(follow));
            curBlock = elseBlk;
            analyseStmt(ifStmt.stmt2);
        } else {
            cur.append(new Branch(cond, then, follow));
            curBlock = then;
            analyseStmt(ifStmt.stmt1);
        }
        curBlock.append(new Jump(follow));
        curBlock = follow;
    }

    private void analyseWhileStmt(WhileStmt whileStmt)  {
        BasicBlock cur = curBlock;
        BasicBlock follow = new BasicBlock("B_"+newBlockCnt(), BlockType.BASIC);
        BasicBlock body = new BasicBlock("LOOP_"+newBlockCnt(), BlockType.BASIC);
        BasicBlock loop = new BasicBlock("WHILE_"+newBlockCnt(), BlockType.LOOP);
        cur.append(new Jump(loop));
        loopBlocks.push(loop);
        loopFollows.push(follow);
        curBlock = loop;
        Operand cond = analyseCond(whileStmt.cond);
        curBlock.append(new Branch(cond,body,follow));
        curBlock = body;
        analyseStmt(whileStmt.stmt);
        loopFollows.pop();
        loopBlocks.pop();
        curBlock.append(new Jump(loop));
        curBlock = follow;
    }

    private void analyseStmt(Stmt stmt)  {
        if (stmt.stmtType.equals(StmtType.EMPTY))
            return;
        if (stmt.stmtType.equals(StmtType.SIMPLE)) {// is simple stmt
            SplStmt splStmt = stmt.splStmt;
            if (splStmt instanceof AssignStmt)
                analyseAssignStmt((AssignStmt) splStmt);
            else if (splStmt instanceof BreakStmt)
                analyseBreakStmt();
            else if (splStmt instanceof ContinueStmt)
                analyseContinueStmt();
            else if (splStmt instanceof ExpStmt)
                analyseExpStmt((ExpStmt) splStmt);
            else if (splStmt instanceof GetIntStmt)
                analyseGetIntStmt((GetIntStmt) splStmt);
            else if (splStmt instanceof ReturnStmt)
                analyseReturnStmt((ReturnStmt) splStmt);
            else if (splStmt instanceof PrintStmt)
                analysePrintStmt((PrintStmt) splStmt);
            else
                throw new Error("SplStmt wrong type");
        } else if (stmt.stmtType.equals(StmtType.COMPLEX)) { // is complex stmt
            CplStmt cplStmt = stmt.cplStmt;
            if (cplStmt instanceof Block)
                analyseBlock((Block) cplStmt);
            else if (cplStmt instanceof IfStmt)
                analyseIfStmt((IfStmt) cplStmt);
            else if (cplStmt instanceof WhileStmt)
                analyseWhileStmt((WhileStmt) cplStmt);
            else
                throw new Error("CplStmt wrong type");
        } else
            throw new Error("Stmt wrong type");

    }

    private BasicBlock analyseBlock(Block block)  {
        BasicBlock basicBlock = new BasicBlock("B_"+newBlockCnt(), BlockType.BASIC);
        if (Objects.nonNull(curBlock))
            curBlock.append(new Jump(basicBlock));
        curBlock = basicBlock;
        BasicBlock follow = new BasicBlock("B_"+newBlockCnt(), BlockType.BASIC);
        //get in the block, new symTab based on symTab
        curSymTab = new SymTab(basicBlock.label, curSymTab);
        blockDep++;

        //analyse evey stmt
        Iterator<BlockItem> blockItemIt = block.blockItemIt();
        while (blockItemIt.hasNext()) {
            BlockItem blockItem = blockItemIt.next();
            if (blockItem instanceof Decl)
                analyseDecl((Decl) blockItem);
            else if (blockItem instanceof Stmt)
                analyseStmt((Stmt) blockItem);
            else
                throw new Error("BlockItem wrong refType!");
        }

        //get out block
        curBlock.append(new Jump(follow));
        curBlock = follow;
        curSymTab = curSymTab.father;
        blockDep--;
        return basicBlock;
    }

    private void analyseBody(Block body, FuncScope funcScope)  {
        // change symTab and stack
        curSymTab = funcScope.symTab;
        stackSize = funcScope.symTab.capacity;
        BasicBlock block = analyseBlock(body);
        BasicBlock funcBody = new BasicBlock(funcScope.label, BlockType.FUNC);
        funcBody.append(new Jump(block));
        curBlock.append(Return.VoidReturn());
        curBlock = null;
        funcScope.body = funcBody;
        //out of func, change back symTab and stack to func's father
        curSymTab = curSymTab.father;
        curFunc = null;
    }

    private void analyseFuncDef(FuncDef funcDef)  {
        String name = funcDef.ident.name;
        ReturnType returnType;
        if (funcDef.funcType.type.tokenType.equals(TokenType.VOIDTK))
            returnType = ReturnType.VOID;
        else
            returnType = ReturnType.INT;
        FuncScope funcScope = FuncScope.Function(name, returnType, curSymTab);
        middleCode.addFunc(funcScope);
        curFunc = funcScope;

        if (Objects.nonNull(funcDef.params)) {
            analyseFuncParams(funcDef.params.first, funcScope);
            Iterator<FuncParam> paramIt = funcDef.params.params.iterator();
            while (paramIt.hasNext())
                analyseFuncParams(paramIt.next(), funcScope);
        }
        analyseBody(funcDef.block, funcScope);
    }

    public void analyseCompUnit(CompUnit compUnit)  {
        //analyse decl
        Iterator<Decl> declIt = compUnit.decls.iterator();
        while (declIt.hasNext())
            analyseDecl(declIt.next());
        //analyse FuncDef
        Iterator<FuncDef> funcDefIt = compUnit.funcDefs.iterator();
        while (funcDefIt.hasNext())
            analyseFuncDef(funcDefIt.next());
        //analyse mainFunc
        FuncScope mainFunc = FuncScope.MainFunction(curSymTab);
        curFunc = mainFunc;
        middleCode.addFunc(mainFunc);
        MainFuncDef mainFuncDef = compUnit.mainFuncDef;
        analyseBody(mainFuncDef.block, mainFunc);
        middleCode.addMainFunc(mainFunc);
    }

    static class Calculator {
        static int calLVal(LVal lVal, SymTab symTab){
            String name = lVal.ident.name;
            Symbol symbol = symTab.getSym(name, true);
            if (symbol.refType.equals(RefType.ITEM))
                return symbol.initialVal;
            if (!symbol.refType.equals(RefType.ARRAY))
                throw new Error("symbol is not array");
            List<Integer> index = new ArrayList<>();
            Iterator<Index> indexIterator = lVal.indexList.iterator();
            while(indexIterator.hasNext())
                index.add(calExp(indexIterator.next().index, symTab));
            int base=1;
            int offset = 0;
            for(int i=index.size()-1;i>=0;i--){
                offset+=index.get(i)*base;
                if (i==0)
                    break;
                base *= symbol.dimSize.get(i);
            }
            return symbol.initialArr.get(offset);
        }

        private static int calSubExp(SubExp subExp, SymTab symTab) {
            return calExp(subExp.exp, symTab);
        }

        private static int calUnaryExp(UnaryExp unaryExp, SymTab symTab) {
            BaseUnaryExp baseUnaryExp = unaryExp.baseUnaryExp;
            int ans;
            BasePrimaryExp basePrimaryExp = ((PrimaryExp)baseUnaryExp).basePrimaryExp;
            if (basePrimaryExp instanceof SubExp)
                ans = calSubExp((SubExp) basePrimaryExp, symTab);
            else if (basePrimaryExp instanceof LVal)
                ans = calLVal((LVal) basePrimaryExp, symTab);
            else
                ans = ((Number) basePrimaryExp).number.value;
            Iterator<Token> it = unaryExp.unaryOp.ops.iterator();
            while(it.hasNext()){
                TokenType type = it.next().tokenType;
                if (type.equals(TokenType.MINU))
                    ans = -ans;
                else if (type.equals(TokenType.NOT))
                    ans = (ans==0)? 1 : 0;
            }
            return ans;
        }

        private static int calMulExp(MulExp mulExp, SymTab symTab) {
            int ans = 0 ;
            ans += calUnaryExp(mulExp.first, symTab);
            Iterator<Token> opIt = mulExp.ops.iterator();
            Iterator<UnaryExp> operandIt = mulExp.operands.iterator();
            while(opIt.hasNext() && operandIt.hasNext()){
                TokenType type = opIt.next().tokenType;
                int operandVal = calUnaryExp(operandIt.next(), symTab);
                if (type.equals(TokenType.MULT))
                    ans *= operandVal;
                else if (type.equals(TokenType.DIV))
                    ans /= operandVal;
                else if (type.equals(TokenType.MOD))
                    ans %= operandVal;
            }
            return ans;
        }

        private static int calAddExp(AddExp addExp, SymTab symTab) {
            int ans =0 ;
            ans += calMulExp(addExp.first, symTab);
            Iterator<Token> opIt = addExp.ops.iterator();
            Iterator<MulExp> operandIt = addExp.operands.iterator();
            while (opIt.hasNext() && operandIt.hasNext()){
                TokenType type = opIt.next().tokenType;
                int operandVal = calMulExp(operandIt.next(), symTab);
                if (type.equals(TokenType.PLUS))
                    ans += operandVal;
                else if (type.equals(TokenType.MINU))
                    ans -= operandVal;
            }
            return ans;
        }

        static int calExp(Exp exp, SymTab symTab) {
            return calAddExp(exp.addExp, symTab);
        }
    }

    static class Transfer { // transfer op and printType
        static final HashMap<TokenType, BinaryOp> TBMap;
        static final HashMap<TokenType, UnaryOp> TUMap;
        static{
            TBMap = new HashMap<>();
            TBMap.put(TokenType.PLUS, BinaryOp.ADD);
            TBMap.put(TokenType.MINU, BinaryOp.SUB);
            TBMap.put(TokenType.MULT, BinaryOp.MUL);
            TBMap.put(TokenType.DIV, BinaryOp.DIV);
            TBMap.put(TokenType.MOD, BinaryOp.MOD);
            TBMap.put(TokenType.AND, BinaryOp.ANDL);
            TBMap.put(TokenType.OR, BinaryOp.ORL);
            TBMap.put(TokenType.GEQ, BinaryOp.GE);
            TBMap.put(TokenType.GRE, BinaryOp.GT);
            TBMap.put(TokenType.LEQ, BinaryOp.LE);
            TBMap.put(TokenType.LSS, BinaryOp.LT);
            TBMap.put(TokenType.EQL, BinaryOp.EQ);
            TBMap.put(TokenType.NEQ, BinaryOp.NE);
            TUMap = new HashMap<>();
            TUMap.put(TokenType.PLUS, UnaryOp.MOVE);
            TUMap.put(TokenType.MINU, UnaryOp.NEG);
            TUMap.put(TokenType.NOT, UnaryOp.NOT);
        }
    }
}