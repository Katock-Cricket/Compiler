package Error;

import Generate.middle.SymTab;
import Generate.middle.code.BasicType;
import Generate.middle.code.FuncScope;
import Generate.middle.code.RefType;
import Generate.middle.code.ReturnType;
import Generate.middle.operand.Immediate;
import Generate.middle.operand.Operand;
import Generate.middle.operand.Symbol;
import Parse.CompUnit;
import Parse.Printer;
import Parse.decl.*;
import Parse.expr.multi.*;
import Parse.expr.unary.Number;
import Parse.expr.unary.*;
import Parse.func.ArrayDim;
import Parse.func.FirstDim;
import Parse.func.FuncDef;
import Parse.func.FuncParam;
import Parse.stmt.cpl.*;
import Parse.stmt.spl.*;
import Tokenize.token.FormatString;
import Tokenize.token.Ident;
import Tokenize.token.Token;
import Tokenize.token.TokenType;

import java.util.*;

import static Generate.Analyzer.get1DimVals;
public class ErrorDetector {
    SymTab curSymTab;
    public LinkedList<Error> errors;
    private final HashMap<String, FuncScope> funcTab;
    private FuncScope curFunc;
    private boolean inFunc;
    private int blockDep, loopDep;

    public ErrorDetector(){
        curSymTab = new SymTab("GLOBAL", null);
        errors = new LinkedList<>();
        inFunc = false;
        curFunc = null;
        funcTab = new HashMap<>();
        blockDep = loopDep = 0;
    }

    private void checkArray(Def def){
        List<Integer> arrayDims = new LinkedList<>();
        for (ArrayDef arrayDef : def.arrayDefs) { // read all dims, calc the size
            if (Objects.isNull(arrayDef.RBk))
                errors.add(new Error('k', def.ident.lineNumber));
            lValChecker.checkExp(arrayDef.constExp, curSymTab, errors);
            arrayDims.add(1);
        }
        if (def.isConst)
            curSymTab.addSym(Symbol.ConstArray(def.ident.name, arrayDims, Collections.emptyList()));
        else
            curSymTab.addSym(Symbol.Array(def.ident.name, arrayDims));
        if (def.isInitialized()) { // initialized array
            ArrayInitVal arrayInitVal = (ArrayInitVal) def.initVal;
            if (!arrayInitVal.isConst && inFunc)
                for (Exp exp : get1DimVals(arrayInitVal))
                    checkExp(exp);
        }
    }

    private void checkVar(Def def){
        if (def.isConst)
            curSymTab.addSym(Symbol.ConstInt(def.ident.name, 1));
        else
            curSymTab.addSym(Symbol.Int(def.ident.name));
        if (def.isInitialized()) {
            ExpInitVal expInitVal = (ExpInitVal) def.initVal; // RVal of def
            if (expInitVal.isConst) // is const RVal
               lValChecker.checkExp(expInitVal.exp, curSymTab, errors);
            else  //is var RVal
                if (!inFunc)
                    lValChecker.checkExp(expInitVal.exp, curSymTab, errors);
                else
                    checkExp(expInitVal.exp);
        }
    }

    private void checkDef(Def def){
        Ident ident = def.ident;
        String name = ident.name;
        // duplicated with symTab
        if (curSymTab.contains(name, false)) {
            errors.add(new Error('b', ident.lineNumber));
            return;
        }
        // duplicated with funcParams
        if (blockDep == 1 && inFunc && curFunc.symTab.contains(name, false)) {
            errors.add(new Error('b', ident.lineNumber));
            return;
        }

        if (def.arrayDefs.isEmpty()) //is single var
            checkVar(def);
        else // is array
            checkArray(def);
    }

    private void checkDecl(Decl decl){
        if (Objects.isNull(decl.divider)) // missing divider ";"
            errors.add(new Error('i', decl.bType.lineNumber));
        checkDef(decl.first);
        for (Def def : decl.defs) checkDef(def);
    }

    private void checkFuncParams(FuncParam funcParam, FuncScope funcScope){
        String name = funcParam.ident.name;
        //duplicated name
        if (funcScope.symTab.contains(name, false)) {
            errors.add(new Error('b', funcParam.ident.lineNumber));
            return;
        }
        if (Objects.nonNull(funcParam.first)) { //is array
            FirstDim firstDim = funcParam.first;
            ArrayList<Integer> dims = new ArrayList<>();
            //firstDim have no RBk
            if (Objects.isNull(firstDim.RBk))
                errors.add(new Error('k', firstDim.LBk.lineNumber));
            for (ArrayDim arrayDim : funcParam.dims) {
                //arrayDim have no Rbk
                if (Objects.isNull(arrayDim.RBk))
                    errors.add(new Error('k', arrayDim.LBk.lineNumber));
                lValChecker.checkExp(arrayDim.length, curSymTab, errors);
                dims.add(1);
            }
            funcScope.symTab.addSym(Symbol.Pointer(name, dims));
            funcScope.params.add(Symbol.Pointer(name, dims));
        }
        else {
            funcScope.params.add(Symbol.Int(name));
            funcScope.symTab.addSym(Symbol.Int(name));
        }
    }

    private Operand checkUnaryExp(UnaryExp unaryExp) {
        BaseUnaryExp baseUnaryExp = unaryExp.baseUnaryExp;
        Operand ret = null;
        // is funcCall
        if (baseUnaryExp instanceof FuncCall) {
            FuncCall funcCall = (FuncCall) baseUnaryExp;
            Ident ident = funcCall.name;
            //missing RP
            if (Objects.isNull(funcCall.RP))
                errors.add(new Error('j', ident.lineNumber));
            //undefined name
            if (!funcTab.containsKey(ident.name)) {
                errors.add(new Error('c', ident.lineNumber));
                return new Immediate(0);
            }
            //check params
            List<Operand> params = new LinkedList<>();
            if (Objects.nonNull(funcCall.funcRParams)) {// have params
                FuncRParams funcParams = funcCall.funcRParams;
                params.add(checkExp(funcParams.first));
                for (Exp param : funcParams.params) 
                    params.add(checkExp(param));
            }
            //check paramCnt if match with when it is defined
            boolean err = false;
            FuncScope funcScope = funcTab.get(ident.name);
            List<Symbol> paramSym = funcScope.params;
            if (params.size() != paramSym.size()) {
                errors.add(new Error('d', ident.lineNumber));
                err = true;
            }
            //check paramType if match with when it is defined
            else {
                Iterator<Operand> paramIt = params.iterator();//call
                Iterator<Symbol> argIt = paramSym.iterator();//define
                while (paramIt.hasNext() && argIt.hasNext()) {
                    Operand param = paramIt.next();
                    Symbol arg = argIt.next();
                    if (Objects.isNull(param)) {
                        err = true;
                        errors.add(new Error('e', ident.lineNumber));
                        break;
                    } else if (param instanceof Immediate) {
                        if (!arg.refType.equals(RefType.ITEM)) {
                            err = true;
                            errors.add(new Error('e', ident.lineNumber));
                            break;
                        }
                    } else {
                        if (!((Symbol) param).refType.equals(arg.refType)) {
                            err = true;
                            errors.add(new Error('e', ident.lineNumber));
                            break;
                        }
                    }
                }
            }
            //get ret sym
            if (funcScope.retType.equals(ReturnType.VOID))
                return null;
            else
                if (!err)
                    ret = Symbol.temp(BasicType.INT, RefType.ITEM);
                else
                    return new Immediate(0);
        }
        //is primaryExp
        else if (baseUnaryExp instanceof PrimaryExp) {
            ret = checkBasePrimaryExp(((PrimaryExp) baseUnaryExp).basePrimaryExp, false);
        }
        //check unaryOp
        for (Token ignored : unaryExp.unaryOp.ops) {
            ret = Symbol.temp(BasicType.INT, RefType.ITEM);
        }
        return ret;
    }

    private Operand checkBUExp(Printer BUExp) {
        if (BUExp instanceof MultiExp)
            return checkBinaryExp((MultiExp) BUExp);
        else if(BUExp instanceof UnaryExp)
            return checkUnaryExp((UnaryExp) BUExp);
        else
            throw new java.lang.Error("BinaryExp Exception");
    }

    private Operand checkBinaryExp(MultiExp multiExp) {
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
            throw new java.lang.Error("MultiExp Exception");
        //check first
        Operand ret = checkBUExp(first);
        if (Objects.isNull(ret))
            return null;
        //check operands
        while (opIt.hasNext() && operandIt.hasNext()) {
            Printer operand = (Printer) operandIt.next();
            if (Objects.isNull(checkBUExp(operand)))
                return null;
            ret = Symbol.temp(BasicType.INT, RefType.ITEM);
        }
        return ret;
    }

    private Operand checkExp(Exp exp) {
        return checkBinaryExp(exp.addExp);
    }

    private boolean checkLAndExp(LAndExp lAndExp) {
        //check first
        if (Objects.isNull(checkBinaryExp(lAndExp.first)))
            return false;
        //check operand
        for (EqExp eqExp : lAndExp.operands)
            if (Objects.isNull(checkBinaryExp(eqExp)))
                return false;
        return true;
    }

    private void checkLOrExp(LOrExp lOrExp) {
        //check first
        if (!checkLAndExp(lOrExp.first))
            return;
        //check operands
        for (LAndExp lAndExp : lOrExp.operands)
            checkLAndExp(lAndExp);
    }

    private Operand checkBasePrimaryExp(BasePrimaryExp basePrimaryExp, boolean isLVal) {
        //is subExp
        if (basePrimaryExp instanceof SubExp) {
            SubExp subExp = (SubExp) basePrimaryExp;
            if (Objects.isNull(subExp.RP)) // missing RP
                errors.add(new Error('j', subExp.LP.lineNumber));
            return checkExp(subExp.exp);
        }
        //is lVal (array or single var)
        else if (basePrimaryExp instanceof LVal) {
            LVal lVal = (LVal) basePrimaryExp;
            // undefined symbol
            if (!curSymTab.contains(lVal.ident.name, true)) {
                errors.add(new Error('c', lVal.ident.lineNumber));
                return new Immediate(0);
            }
            Symbol lValSym = curSymTab.getSym(lVal.ident.name, true);
            RefType refType = lValSym.refType;
            List<Operand> indexes = new LinkedList<>();//index can be imm or sym
            if (!lVal.indexList.isEmpty() && refType.equals(RefType.ITEM))
                throw new java.lang.Error("int symbol has index");

            for (Index index : lVal.indexList) {
                if (Objects.isNull(index.RBk)) {
                    errors.add(new Error('k', index.LBk.lineNumber));
                    return new Immediate(0);
                }
                indexes.add(new Operand() {});
            }
            int indexCnt = indexes.size(), dimCnt = lValSym.dimSize.size();
            //index must be <= dims
            if (indexCnt > dimCnt && refType.equals(RefType.ARRAY) ||
                    indexCnt > dimCnt + 1 && refType.equals(RefType.POINTER))
                throw new java.lang.Error("indexes more than dim");

            if (refType.equals(RefType.ITEM))//is single var
                return lValSym;
            else if (refType.equals(RefType.ARRAY)) {//is array
                Symbol pointer = lValSym.toPointer(1).toPointer(indexCnt);
                if (isLVal || indexCnt < lValSym.dimSize.size())
                    return pointer;
                else {
                    return Symbol.temp(BasicType.INT, RefType.ITEM);
                }
            }
            else {//is pointer
                Symbol pointer = lValSym.toPointer(indexCnt);
                if (isLVal || indexCnt <= lValSym.dimSize.size())
                    return pointer;
                else {
                    return Symbol.temp(BasicType.INT, RefType.ITEM);
                }
            }
        }
        else if (basePrimaryExp instanceof Number)
            return new Immediate(((Number) basePrimaryExp).number.value);
        else
            throw new java.lang.Error("BasePrimaryExp Exception");
    }

    private void checkLVal(LVal lVal) {
        Operand lValSym = checkBasePrimaryExp(lVal, true);
        if (Objects.isNull(lValSym) || !(lValSym instanceof Symbol))
            return;
        if (((Symbol) lValSym).isConst)
            errors.add(new Error('h', lVal.ident.lineNumber));
    }
    private int getParamsCntFormatString(FormatString formatString) {
        char[] string = formatString.inner.toCharArray();
        int len = string.length;
        int cnt = 0;
        for (int i = 0; i < len; i++) {
            char c = string[i];
            if (c != 32 && c != 33 && !(c >= 40 && c <= 126)) {
                if (c == '%') {
                    if (i < len - 1 && string[i + 1] == 'd') {
                        cnt++;
                        continue;
                    } else
                        return -1;
                } else
                    return -1;
            }
            if (c == 92 && (i >= len - 1 || string[i + 1] != 'n')) {
                return -1;
            }
        }
        return cnt;
    }
    private void checkPrintStmt(PrintStmt printStmt) {
        int paramsInStr = getParamsCntFormatString(printStmt.formatString);
        if (paramsInStr < 0) {
            errors.add(new Error('a', printStmt.lineNumber()));
            return;
        }
        int paramsInOperand = printStmt.params.size();
        for (Exp exp : printStmt.params)
            checkExp(exp);// check all the exps
        if (paramsInOperand != paramsInStr)
            errors.add(new Error('l', printStmt.lineNumber()));
    }

    private void checkIfStmt(IfStmt ifStmt){
        //missing RP
        if (Objects.isNull(ifStmt.RP))
            errors.add(new Error('j', ifStmt.LP.lineNumber));

        if (Objects.nonNull(ifStmt._else) && Objects.nonNull(ifStmt.stmt2)) {// have else
            checkStmt(ifStmt.stmt1);
            checkStmt(ifStmt.stmt2);
        }
        else
            checkStmt(ifStmt.stmt1);
    }

    private void checkWhileStmt(WhileStmt whileStmt){
        // missing RP
        if (Objects.isNull(whileStmt.RP))
            errors.add(new Error('j', whileStmt.LP.lineNumber));
        checkLOrExp(whileStmt.cond.lOrExp);
        loopDep++;
        checkStmt(whileStmt.stmt);
        loopDep--;
    }
    
    private void checkReturnStmt(ReturnStmt returnStmt){
        // return must in a func
        if (Objects.isNull(curFunc))
            throw new java.lang.Error("Return not in func");
        if (curFunc.retType.equals(ReturnType.INT))
            if (Objects.isNull((returnStmt).exp))
                throw new java.lang.Error("int func should have return val");
        if (curFunc.retType.equals(ReturnType.VOID) && Objects.nonNull(returnStmt.exp))
            errors.add(new Error('f', returnStmt._return.lineNumber));
    }

    private void checkStmt(Stmt stmt){
        if (stmt.stmtType.equals(StmtType.EMPTY))
            return;
        if (stmt.stmtType.equals(StmtType.SIMPLE)) {// is simple stmt
            // missing divider ";"
            SplStmt splStmt = stmt.splStmt;
            if (Objects.isNull(stmt.divider))
                errors.add(new Error('i', splStmt.lineNumber()));

            if (splStmt instanceof AssignStmt) {
                checkLVal(((AssignStmt) splStmt).lVal);
                if (Objects.isNull(checkExp(((AssignStmt) splStmt).exp)))
                    throw new java.lang.Error("Assign void to LVal");
            }
            else if (splStmt instanceof ContinueStmt || splStmt instanceof BreakStmt) {
                if (loopDep == 0)
                    errors.add(new Error('m', splStmt.lineNumber()));
            }
            else if (splStmt instanceof ExpStmt)
                checkExp(((ExpStmt) splStmt).exp);
            else if (splStmt instanceof GetIntStmt)
                checkLVal(((GetIntStmt) splStmt).lVal);
            else if (splStmt instanceof ReturnStmt) {
                checkReturnStmt((ReturnStmt)splStmt);
            }
            else if (splStmt instanceof PrintStmt)
                checkPrintStmt((PrintStmt) splStmt);
            else
                throw new java.lang.Error("SplStmt wrong type");
        } else if (stmt.stmtType.equals(StmtType.COMPLEX)) { // is complex stmt
            CplStmt cplStmt = stmt.cplStmt;
            if (cplStmt instanceof Block)
                checkBlock((Block) cplStmt);
            else if (cplStmt instanceof IfStmt)
                checkIfStmt((IfStmt) cplStmt);
            else if (cplStmt instanceof WhileStmt)
                checkWhileStmt((WhileStmt) cplStmt);
            else
                throw new java.lang.Error("CplStmt wrong type");
        }else
            throw new java.lang.Error("Stmt wrong type");
    }

    private void checkBlock(Block block) {
        //get in the block, new symTab based on symTab
        curSymTab = new SymTab("B"+blockDep, curSymTab);
        blockDep++;
        //check evey stmt
        Iterator<BlockItem> blockItemIt = block.blockItemIt();
        while (blockItemIt.hasNext()) {
            BlockItem blockItem = blockItemIt.next();
            if (blockItem instanceof Decl)
                checkDecl((Decl) blockItem);
            else if (blockItem instanceof Stmt)
                checkStmt((Stmt) blockItem);
            else
                throw new java.lang.Error("BlockItem wrong refType!");
        }
        //get out block
        curSymTab = curSymTab.father;
        blockDep--;
    }

    private void checkBody(Block body, FuncScope funcScope){
        // change symTab
        curSymTab = funcScope.symTab;
        inFunc = true;
        checkBlock(body);
        //check if missing "return" stmt
        Iterator<BlockItem> blockItemIt = body.blockItemIt();
        boolean haveReturn = false;
        while (blockItemIt.hasNext()) {
            BlockItem blockItem = blockItemIt.next();
            if (!blockItemIt.hasNext())
                if (blockItem instanceof Stmt &&
                        ((Stmt) blockItem).stmtType.equals(StmtType.SIMPLE) &&
                        ((Stmt) blockItem).splStmt instanceof ReturnStmt)
                    haveReturn = true;
        }
        if (!(haveReturn || funcScope.retType.equals(ReturnType.VOID))) //is int && no return
            errors.add(new Error('g', body.RB.lineNumber));
        //out of func, change back symTab and stack to func's father
        curSymTab = curSymTab.father;
        curFunc = null;
        inFunc = false;
    }

    private void checkFuncDef(FuncDef funcDef){
        // missing RP
        if (Objects.isNull(funcDef.RP))
            errors.add(new Error('j', funcDef.ident.lineNumber));
        String name = funcDef.ident.name;
        // duplicated funcName
        if (funcTab.containsKey(name) || curSymTab.contains(name, false)) {
            errors.add(new Error('b', funcDef.ident.lineNumber));
            return;
        }
        ReturnType returnType;
        if (funcDef.funcType.type.tokenType.equals(TokenType.VOIDTK))
            returnType = ReturnType.VOID;
        else
            returnType = ReturnType.INT;
        FuncScope funcScope = FuncScope.Function(name, returnType, curSymTab);
        funcTab.put(name, funcScope);
        curFunc = funcScope;

        if (Objects.nonNull(funcDef.params)) {
            checkFuncParams(funcDef.params.first, funcScope);
            for (FuncParam funcParam : funcDef.params.params)
                checkFuncParams(funcParam, funcScope);
        }
        checkBody(funcDef.block, funcScope);
    }

    public void checkCompUnit(CompUnit compUnit)  {
        //check decl
        for (Decl decl : compUnit.decls)
            checkDecl(decl);
        //check FuncDef
        for (FuncDef funcDef : compUnit.funcDefs)
            checkFuncDef(funcDef);
        //check mainFunc
        FuncScope mainFunc = FuncScope.MainFunction(curSymTab);
        curFunc = mainFunc;
        checkBody(compUnit.mainFuncDef.block, mainFunc);
    }

    static class lValChecker {
        static void checkLVal(LVal lVal, SymTab symTab, LinkedList<Error> errors){
            String name = lVal.ident.name;
            if (!symTab.contains(name, true)){
                errors.add(new Error('c', lVal.ident.lineNumber));
                return;
            }
            Symbol symbol = symTab.getSym(name, true);
            if (!symbol.isConst)
                throw new java.lang.Error("symbol is not const");
            if (symbol.refType.equals(RefType.ITEM))
                return;
            if (!symbol.refType.equals(RefType.ARRAY))
                throw new java.lang.Error("symbol is not array");
            
            for (Index value : lVal.indexList)
                checkExp(value.index, symTab, errors);
        }

        private static void checkUnaryExp(UnaryExp unaryExp, SymTab symTab, LinkedList<Error> errors){
            BaseUnaryExp baseUnaryExp = unaryExp.baseUnaryExp;
            BasePrimaryExp basePrimaryExp = ((PrimaryExp)baseUnaryExp).basePrimaryExp;
            if (basePrimaryExp instanceof SubExp)
                checkExp(((SubExp) basePrimaryExp).exp, symTab, errors);
            else if (basePrimaryExp instanceof LVal)
                checkLVal((LVal) basePrimaryExp, symTab, errors);
        }

        private static void checkMulExp(MulExp mulExp, SymTab symTab, LinkedList<Error> errors){
            checkUnaryExp(mulExp.first, symTab, errors);
            Iterator<Token> opIt = mulExp.ops.iterator();
            Iterator<UnaryExp> operandIt = mulExp.operands.iterator();
            while(opIt.hasNext() && operandIt.hasNext())
                checkUnaryExp(operandIt.next(), symTab, errors);
        }

        private static void checkAddExp(AddExp addExp, SymTab symTab, LinkedList<Error> errors){
            checkMulExp(addExp.first, symTab, errors);
            Iterator<Token> opIt = addExp.ops.iterator();
            Iterator<MulExp> operandIt = addExp.operands.iterator();
            while (opIt.hasNext() && operandIt.hasNext())
                checkMulExp(operandIt.next(), symTab, errors);
        }

        static void checkExp(Exp exp, SymTab symTab, LinkedList<Error> errors){
            checkAddExp(exp.addExp, symTab, errors);
        }
    }
}
