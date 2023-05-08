package Parse.stmt.spl;

import Parse.expr.multi.Exp;
import Parse.expr.unary.*;
import Parse.expr.unary.Number;

import java.io.PrintStream;

public class ExpStmt implements SplStmt{
    public final Exp exp;

    public ExpStmt(Exp exp1){
        exp=exp1;
    }

    @Override
    public int lineNumber(){
        BaseUnaryExp baseUnaryExp = exp.addExp.first.first.baseUnaryExp;
        if (baseUnaryExp instanceof FuncCall)
            return ((FuncCall) baseUnaryExp).name.lineNumber;
        else if (baseUnaryExp instanceof PrimaryExp) {
            BasePrimaryExp basePrimaryExp = ((PrimaryExp) baseUnaryExp).getBasePrimaryExp();
            if(basePrimaryExp instanceof SubExp)
                return ((SubExp) basePrimaryExp).LP.lineNumber;
            else if (basePrimaryExp instanceof LVal)
                return ((LVal) basePrimaryExp).ident.lineNumber;
            else if (basePrimaryExp instanceof Number)
                return ((Number) basePrimaryExp).number.lineNumber;
            else
                throw new AssertionError("PrimaryExp wrong type!");
        }
        else
            throw new AssertionError("Unary wrong type!");
    }

    @Override
    public void output(PrintStream p){
        exp.output(p);
    }
}
