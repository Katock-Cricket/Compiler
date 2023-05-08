package Generate.middle.code;

public class PrintStr extends Node{
    public final String label;

    public PrintStr(String s){
        label=s;
    }

    @Override
    public String toString() {
        return "PRINT_STR "+label;
    }
}
