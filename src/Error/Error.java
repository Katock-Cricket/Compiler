package Error;

public class Error implements Comparable<Error> {

    public final char type;
    public final int lineNum;

    public Error(char type, int lineNum) {
        this.type = type;
        this.lineNum = lineNum;
    }

    @Override
    public String toString() {
        return lineNum + " " + type;
    }

    @Override
    public int compareTo(Error o) {
        return Integer.compare(lineNum, o.lineNum);
    }
}