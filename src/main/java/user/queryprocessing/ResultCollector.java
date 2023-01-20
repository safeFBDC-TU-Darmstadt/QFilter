package user.queryprocessing;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public abstract class ResultCollector {

    public static BigInteger getCountResult(List<BigInteger> results) {
        return interpolate(results);
    }

    public static String getSumResultBase10(List<BigInteger[][]> results, Class<?> resultType, int decimalPlaces) {
        final int digitsPerValue = results.get(0).length;
        final int sharesPerDigit = results.get(0)[0].length;

        BigInteger[][] translatedResult = new BigInteger[digitsPerValue][sharesPerDigit];

        for (int i = 0; i < digitsPerValue; i++) {
            for (int j = 0; j < sharesPerDigit; j++) {
                List<BigInteger> unaryValueResult = new ArrayList<>();
                for (BigInteger[][] result : results) {
                    unaryValueResult.add(result[i][j]);
                }
                translatedResult[i][j] = interpolate(unaryValueResult);
            }
        }

        // 'inverse unary translation' for each digit (but with different counts and place values)
        // example: [[0, 3, 0, 0, 0, 0, 0, 0, 0, 0], [1, 2, 0, 0, 0, 0, 0, 0, 0, 0]] -> 32
        BigInteger result = new BigInteger("0");
        for (int i = 0; i < digitsPerValue; i++) {
            for (int j = 0; j < sharesPerDigit; j++) {
                BigInteger placeValue = new BigInteger(String.valueOf((long) Math.pow(10, digitsPerValue-1-i)));
                BigInteger digitValue = new BigInteger(String.valueOf(j));
                result = result.add(translatedResult[i][j].multiply(placeValue).multiply(digitValue));
            }
        }

        if (resultType == Float.class) {
            BigDecimal decimalResult = new BigDecimal(result);
            decimalResult = decimalResult.divide(new BigDecimal(String.valueOf((long) Math.pow(10, decimalPlaces))), decimalPlaces, RoundingMode.DOWN);
            return String.valueOf(decimalResult);
        }

        return String.valueOf(result);
    }

    public static BigInteger interpolate(List<BigInteger> results) {
        BigInteger sum = BigInteger.valueOf(0);
        for (int i = 1; i <= results.size(); i++) {
            BigInteger counter = BigInteger.valueOf(1);
            BigInteger denominator = BigInteger.valueOf(1);
            for (int j = 1; j <= results.size(); j++) {
                if (i != j) {
                    counter = counter.multiply(BigInteger.valueOf(-j));
                    denominator = denominator.multiply(BigInteger.valueOf(i - j));
                }
            }
            counter = counter.multiply(results.get(i - 1));
            sum = sum.add(counter.divide(denominator));
        }
        return sum;
    }

}
