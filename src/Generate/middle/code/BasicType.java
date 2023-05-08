package Generate.middle.code;

public enum BasicType {

    INT(4), SHORT(2), CHAR(1);

    public final int size;
    BasicType(int size1){
        size=size1;
    }
}
