package Optimize.Mid;

import java.math.BigInteger;

public class MathUtil{
    public static boolean isLog2(int num){
        return (num & (num - 1)) == 0;
    }
    public static int log2(int num){
        return ((Integer.BYTES << 3) - 1) - Integer.numberOfLeadingZeros(num);
    }

    private static String to32BitsBinary(int a){
        String b = Integer.toBinaryString(a);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0;i<32-b.length();i++)
            stringBuilder.append('0');
        stringBuilder.append(b);
        return stringBuilder.toString();
    }

    public static int clo(int a){
        int cnt = 0;
        for(char i : to32BitsBinary(a).toCharArray())
            if (i == '1')
                cnt++;
            else break;
        return cnt;
    }

    public static int clz(int a){
        int cnt = 0;
        for(char i : to32BitsBinary(a).toCharArray())
            if (i == '0')
                cnt++;
            else break;
        return cnt;
    }

    public static long getUnsignedInt(int num) {
        return num & 0xFFFFFFFFL;
    }

    public static int[] divideU64To32(int hi, int lo, int divisor) {
        BigInteger n1 = BigInteger.valueOf(getUnsignedInt(hi)).shiftLeft(32);
        BigInteger n2 = BigInteger.valueOf(getUnsignedInt(lo));
        BigInteger n = n1.or(n2);
        BigInteger d = BigInteger.valueOf(divisor);
        BigInteger[] result = n.divideAndRemainder(d);
        return new int[]{result[0].intValueExact(), result[1].intValueExact()};
    }
}
