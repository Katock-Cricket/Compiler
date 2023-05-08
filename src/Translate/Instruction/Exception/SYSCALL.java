package Translate.Instruction.Exception;

import Translate.Instruction.Instruction;

public class SYSCALL extends Instruction {
    public static final int PRINT_INT = 1,
    PRINT_STR = 4,
    READ_INT = 5,
    TERMINATE = 10;

    @Override
    public String instrToString() {
        return "syscall";
    }
}
