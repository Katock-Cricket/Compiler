import Generate.Analyzer;
import Error.ErrorDetector;
import Generate.middle.code.MiddleCode;
import Optimize.Mid.*;
import Optimize.Mips.MipsOpt;
import Optimize.Mips.DecJ;
import Parse.CompUnit;
import Parse.CompUnitParser;
import Tokenize.Source;
import Tokenize.Tokenizer;
import Translate.Mips;
import Translate.Translator;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Compiler {
    static List<PrintOpt> printOpt = new ArrayList<>();
    static List<Optimize> optimize = new ArrayList<>();
    static Source source;
    static Tokenizer tokenizer;
    static CompUnit compUnit;
    static ErrorDetector errorDetector;
    static Analyzer analyzer;
    static MiddleCode middleCode;
    static MidOpt removeUseless = new RemoveAfterJump(),
            blockOpt = new BlockOpt(),
            modOpt = new ModOpt(),
            divOpt = new DivOpt(),
            mulOpt = new MulOpt(),
            decTmp = new DecTmp();
    static MipsOpt removeUselessJ = new DecJ();
    static Translator translator;
    static Mips mips;

    public static void main(String[] args) {
        printOpt.add(PrintOpt.Mips);
//        printOpt.add(PrintOpt.Token);
//        printOpt.add(PrintOpt.Tree);
//        printOpt.add(PrintOpt.MiddleCode);
//        printOpt.add(PrintOpt.Error);
        optimize.add(Optimize.Mid);
        optimize.add(Optimize.Mips);
        long time = System.currentTimeMillis();
        long time2, time3, time4, time5, time6;

        // tokenize
        try {
            source = new Source(new FileInputStream("testfile.txt"));
            tokenizer = new Tokenizer(source);
            time2 = System.currentTimeMillis();
            System.out.println("Tokenize cost: "+(time2-time)+"ms");
            if (printOpt.contains(PrintOpt.Token))
                tokenizer.output(new PrintStream("token.txt"));
            time2 = System.currentTimeMillis();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // syntax
        try {
            compUnit = new CompUnitParser(tokenizer.getTokens().listIterator(), tokenizer.getMaxLineNumber()).parseCompUnit();
            time3 = System.currentTimeMillis();
            System.out.println("Parse cost: "+(time3-time2)+"ms");
            if (printOpt.contains(PrintOpt.Tree))
                compUnit.output(new PrintStream("tree.txt"));
            time3 = System.currentTimeMillis();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        //error
        try {
            errorDetector = new ErrorDetector();
            errorDetector.checkCompUnit(compUnit);
            time4 = System.currentTimeMillis();
            System.out.println("Error cost: "+(time4-time3)+"ms");
            if (printOpt.contains(PrintOpt.Error)) {
                try (PrintStream p = new PrintStream("error.txt")) {
                    errorDetector.errors.forEach(error -> p.println(error.lineNum + " " + error.type));
                }
            }
            time4 = System.currentTimeMillis();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // middleCode
        try {
            analyzer = new Analyzer();
            analyzer.analyseCompUnit(compUnit);
            middleCode = analyzer.middleCode;
            if (optimize.contains(Optimize.Mid)){
                removeUseless.optimize(middleCode);
                blockOpt.optimize(middleCode);
                modOpt.optimize(middleCode);
                divOpt.optimize(middleCode);
                mulOpt.optimize(middleCode);
                decTmp.optimize(middleCode);
            }
            time5 = System.currentTimeMillis();
            System.out.println("Generate cost: "+(time5-time4)+"ms");
            if (printOpt.contains(PrintOpt.MiddleCode)) middleCode.output(new PrintStream("output.txt"));
            time5 = System.currentTimeMillis();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        // mips
        try {
            translator = new Translator(middleCode);
            mips = translator.generateMips();
            if (optimize.contains(Optimize.Mips)){
                removeUselessJ.optimize(mips);
            }
            time6 = System.currentTimeMillis();
            System.out.println("Translate cost: "+(time6-time5)+"ms");
            if (printOpt.contains(PrintOpt.Mips)) mips.output(new PrintStream("mips.txt"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Compile cost: "+(System.currentTimeMillis()-time)+"ms");
    }

    enum PrintOpt {
        Token, Tree, MiddleCode, Error, Mips
    }

    enum Optimize {
        Mid, Mips
    }
}

