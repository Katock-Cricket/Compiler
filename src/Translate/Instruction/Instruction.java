package Translate.Instruction;

import Generate.middle.code.Node;

import java.util.Objects;

public abstract class Instruction extends Node {
    public String label, description;

    public Instruction(){
        label = description = null;
    }

    public abstract String instrToString();

    public static Instruction nop(){
        return new Instruction() {
            @Override
            public String instrToString() {
                return "";
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (Objects.nonNull(label))
            stringBuilder.append(label).append(":\n");
        stringBuilder.append(instrToString());
        return stringBuilder.toString();
    }
}
