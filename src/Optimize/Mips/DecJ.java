package Optimize.Mips;

import Translate.Instruction.BranchJump.J;
import Translate.Instruction.BranchJump.JAL;
import Translate.Instruction.Instruction;
import Translate.Instruction.SST;
import Translate.Instruction.ST;
import Translate.Mips;

import java.util.Objects;

/*
    if (not a label)
        j label
        label:
        bala bala
    then remove j label, because is next
    else if label1:
            j label2
    then remove j, replace all label1 with label2
 */

public class DecJ implements MipsOpt{

    public DecJ(){}

    @Override
    public void optimize(Mips mips) {
        for (Instruction instr = (Instruction) mips.entry.next;
             Objects.nonNull(instr) && Objects.nonNull(instr.next);
             instr = (Instruction) instr.next)
            if (instr instanceof J){
                J j = (J) instr;
                Instruction next = (Instruction) j.next;
                if (Objects.isNull(j.label) && Objects.nonNull(next.label) ){
                    if(j.target.equals(next.label))
                        j.remove();
                }
            }
        for (Instruction instr = (Instruction) mips.entry.next;
             Objects.nonNull(instr) && Objects.nonNull(instr.next);
             instr = (Instruction) instr.next)
            if (instr instanceof J){
                J j = (J) instr;
                if (Objects.nonNull(j.label)){
                    for (Instruction instr1 = (Instruction) mips.entry.next;
                         Objects.nonNull(instr1) && Objects.nonNull(instr1.next);
                         instr1 = (Instruction) instr1.next){
                        if (instr1 instanceof J && ((J) instr1).target.equals(j.label))
                            ((J) instr1).target = j.target;
                        else if (instr1 instanceof JAL && ((JAL) instr1).target.equals(j.label))
                            ((JAL) instr1).target = j.target;
                        else if (instr1 instanceof ST) {
                            if (((ST) instr1).target.equals(j.label))
                                ((ST) instr1).target = j.target;
                        }
                        else if(instr1 instanceof SST) {
                            if (((SST) instr1).target.equals(j.label))
                                ((SST) instr1).target = j.target;
                        }
                    }
                    j.remove();
                }
            }
    }
}