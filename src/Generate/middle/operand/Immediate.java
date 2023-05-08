package Generate.middle.operand;

public class Immediate implements Operand{
    public final int value;

    public Immediate(int val){
        value = val;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
