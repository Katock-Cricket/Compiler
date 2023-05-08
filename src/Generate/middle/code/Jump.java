package Generate.middle.code;

public class Jump extends Node{
    public final BasicBlock target;
    private final String op = "Jump";

    public Jump(BasicBlock block){
        this.target=block;
    }

    @Override
    public String toString() {
        return op + " " + target;
    }
}
