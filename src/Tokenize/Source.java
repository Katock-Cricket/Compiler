package Tokenize;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Source {
    private final List<String> lines = new ArrayList<>();
    private int line = 0;
    private int column = 0;

    public Source(InputStream input) {
        BufferedReader br = new BufferedReader(new InputStreamReader(input));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public int getLineIndex() { return line + 1; }

    public String getCurrentLine() {
        if (!reachedEndOfFile()) {
            return lines.get(line);
        } else { return ""; }
    }

    public String getRemainingLine() {
        return getCurrentLine().substring(column);
    }

    public void nextLine() {
        if (!reachedEndOfFile()) {
            line++;
            column = 0;
        }
    }

    public boolean reachedEndOfFile() {
        return line >= lines.size();
    }

    public boolean reachedEndOfLine() {
        return column >= getCurrentLine().length();
    }

    public void forward(int steps) {
        while (!reachedEndOfFile() && steps > 0) {
            int length = getCurrentLine().length();
            if (column + steps > length) {
                line++;
                steps -= (length - column + 1);
                column = 0;
            } else {
                column += steps;
                return;
            }
        }
    }

    public char currentChar() {
        if (reachedEndOfLine()) { return '\n'; }
        return getCurrentLine().charAt(column);
    }

    public boolean skipBlanksComments() {
        while (!reachedEndOfFile() && Character.isWhitespace(currentChar())) {
            forward(1);
        }
        if ("//".equals(followingSeq(2))) {
            nextLine();
            return true;
        }
        if ("/*".equals(followingSeq(2))) {
            forward(2);
            while (!reachedEndOfFile() && !"*/".equals(followingSeq(2))) {
                forward(1);
            }
            if ("*/".equals(followingSeq(2))) {
                forward(2);
            }
            return true;
        }
        return false;
    }

    public String followingSeq(int length) {
        if (reachedEndOfFile()) { return ""; }
        if (column + length >= getCurrentLine().length()) {
            return getCurrentLine().substring(column);
        } else {
            return getCurrentLine().substring(column, column + length);
        }
    }

    public String matchFollowing(Pattern pattern) {
        Matcher matcher = pattern.matcher(getRemainingLine());
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    public void printAll(PrintStream ps) {
        lines.forEach(ps::println);
    }
}
