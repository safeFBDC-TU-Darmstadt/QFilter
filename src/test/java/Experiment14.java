import user.queryprocessing.ResultCollector;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Measures the time to collect (random but realistic) results of count queries for a variable number of query conditions.
public class Experiment14 {

    int[] queryConditions = new int[]{0,0,2,2,4,4};

    @Test
    void measureTime() {
        for (boolean columnPolicy : new boolean[]{false,true}) {
            for (int numConditions : queryConditions) {
                int numServers = getNumberOfServers(numConditions, columnPolicy);
                int numDigits = getNumberOfDigits(numConditions, columnPolicy);
                List<BigInteger> pseudoResults = new ArrayList<>();
                for (int i = 0; i < numServers; i++) {
                    pseudoResults.add(getRandomNumber(numDigits));
                }

                int numRuns = 5;

                long time = 0;
                for (int i = 0; i < numRuns; i++) {
                    long start = System.nanoTime();
                    ResultCollector.getCountResult(pseudoResults);
                    time += System.nanoTime() - start;
                }

                System.out.println("Time (ns) to collect the result (numConditions:"+ numConditions + ", columnPolicy:" + columnPolicy + ", numServers:" + numServers + "): " + time/numRuns);
            }
        }
    }

    int getNumberOfServers(int numConditions, boolean columnPolicy) {
        // assumes count queries
        return columnPolicy ? 2 * 8 * (Math.max(numConditions * 2, 1)) + 1 : 2 * 8 * (numConditions+1) + 1;
    }

    int getNumberOfDigits(int numConditions, boolean columnPolicy) {
        switch (numConditions) {
            case 0 -> { return 26; }
            case 2 -> { return columnPolicy ? 181 : 130; }
            case 4 -> { return columnPolicy ? 419 : 246; }
            default -> { return -1; }
        }
    }

    BigInteger getRandomNumber(int numDigits) {
        StringBuilder number = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < numDigits; i++)
            number.append(r.nextInt(10));
        return new BigInteger(number.toString());
    }

}
