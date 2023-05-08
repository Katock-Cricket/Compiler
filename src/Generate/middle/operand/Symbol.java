package Generate.middle.operand;

import Generate.middle.code.BasicType;
import Generate.middle.code.RefType;

import java.util.*;

public class Symbol implements Operand {
    public static final int sizeOfInt = 4;
    public final String name;
    public final BasicType basicType;
    public final RefType refType;
    public boolean isLocal, isConst;
    public int offset = -1;
    public  List<Integer> dimSize, dimBase;
    public final Integer initialVal;
    public final List<Integer> initialArr;
    private static int tmpCnt=0;

    private Symbol(String name, BasicType basicType, RefType refType,
                  boolean isConst, List<Integer> dimSize, Integer initialVal,
                  List<Integer> initialArr){
        this.name=name;
        this.basicType=basicType;
        this.refType=refType;
        this.isConst=isConst;
        this.dimSize=dimSize;
        this.initialVal=initialVal;
        this.initialArr=initialArr;
        if (refType.equals(RefType.ITEM)) //single var
            this.dimBase = Collections.emptyList();
        else if (refType.equals(RefType.POINTER)) // pointer
            this.dimBase = suffixProduct(dimSize, basicType.size,true);
        else if (Objects.nonNull(dimSize))// array
            this.dimBase = suffixProduct(dimSize, basicType.size,false);
    }

    public static Symbol Int(String name){ // single int without init
        return new Symbol(name,
                BasicType.INT,
                RefType.ITEM,
                false,
                Collections.emptyList(),
                null,
                Collections.emptyList());
    }

    public static Symbol InitializedInt(String name, int initVal){ // single int with init
        return new Symbol(name,
                BasicType.INT,
                RefType.ITEM,
                false,
                Collections.emptyList(),
                initVal,
                Collections.emptyList());
    }

    public static Symbol ConstInt(String name, int initVal){ // single const int
        return new Symbol(name,
                BasicType.INT,
                RefType.ITEM,
                true,
                Collections.emptyList(),
                initVal,
                Collections.emptyList());
    }

    public static Symbol Array(String name, List<Integer> dims){ // array without init
        return new Symbol(name,
                BasicType.INT,
                RefType.ARRAY,
                false,
                Collections.unmodifiableList(dims),
                null,
                Collections.emptyList());
    }

    public static Symbol InitializedArray(String name, List<Integer> dims, List<Integer> initArr){ // array with init
        return new Symbol(name,
                BasicType.INT,
                RefType.ARRAY,
                false,
                Collections.unmodifiableList(dims),
                null,
                Collections.unmodifiableList(initArr));
    }

    public static Symbol ConstArray(String name, List<Integer> dims, List<Integer> initArr){ // const array
        return new Symbol(name,
                BasicType.INT,
                RefType.ARRAY,
                true,
                Collections.unmodifiableList(dims),
                null,
                Collections.unmodifiableList(initArr));
    }

    public static Symbol Pointer(String name, List<Integer> dims){ // pointer (array funcParam)
        return new Symbol(name,
                BasicType.INT,
                RefType.POINTER,
                false,
                Collections.unmodifiableList(dims),
                null,
                Collections.emptyList());
    }

    private static List<Integer> suffixProduct(List<Integer> integerList,
                                               int basicSize, boolean isPointer){
        List<Integer> suffix = new LinkedList<>();
        int prod = basicSize;
        List<Integer> revInput = new ArrayList<>(integerList);
        Collections.reverse(revInput);
        for (Integer integer : revInput) {
            suffix.add(prod);
            prod *= integer;
        }
        if (isPointer)
            suffix.add(prod);
        Collections.reverse(suffix);
        return Collections.unmodifiableList(suffix);
    }

    public int capacity(){
        if (refType.equals(RefType.ITEM) || refType.equals(RefType.POINTER))
            return sizeOfInt;
        if (Objects.nonNull(dimSize))
            return sizeOfInt*dimSize.stream().reduce((i,i2)->(i*i2)).orElse(1);
        return 0;
    }

    public Symbol toPointer(int depth) {
        List<Integer> reducedDimSize = new ArrayList<>();
        for(int i=depth;i< dimSize.size();i++)
            reducedDimSize.add(dimBase.get(i));
        tmpCnt++;
        Symbol symbol = new Symbol("ptr_"+tmpCnt, basicType, RefType.POINTER,
                isConst, Collections.unmodifiableList(reducedDimSize),
                null, Collections.emptyList());
        symbol.isLocal=true;
        return symbol;
    }

    public static Symbol temp(BasicType basicType, RefType refType){
        tmpCnt++;
        Symbol symbol;
        if (refType.equals(RefType.POINTER))
            symbol = Pointer("ptr_"+tmpCnt, Collections.emptyList());
        else
            symbol = new Symbol("tmp_"+tmpCnt, basicType, RefType.ITEM,
                    false, Collections.emptyList(),
                    null, Collections.emptyList());
        symbol.isLocal=true;
        return symbol;
    }

    @Override
    public String toString() {
        return name +
                (offset<0?"(tmp)":String.format(isLocal?"@[sp-0x%x]":"@[data+0x%x]",offset))
                + ":" + refType;
    }
}
