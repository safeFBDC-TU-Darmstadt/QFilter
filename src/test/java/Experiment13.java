import common.ConditionalType;
import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import owner.lineitem.LineitemAttributes;
import server.BatchedFileTable;
import server.measurements.MeasurementServer;

import java.util.*;

// Measures the time to rewrite a query with a variable number of conditions (always combined by AND) and different
// aggregation functions.
public class Experiment13 {

    int[] queryConditions = new int[]{0,0,2,2,4,4};
    int numGroups = 2;
    int numColumns = 4;
    int numRows = 100_000;
    int accessibilityPercentage = 50;

    @BeforeEach
    void init() {
        Map<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put("lineitem", LineitemAttributes.maxStringLength);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMeta);
        SecretCreator.initSecretCreatorSingleton(1);
    }

    @Test
    void measureTime() throws Exception {
        for (boolean columnPolicy : new boolean[]{false, true}) {
            BatchedFileTable table = new BatchedFileTable(Arrays.stream(LineitemAttributes.getAttributes(true, columnPolicy, 4)).toList(), "src/main/resources/lineitem-1.tbl", true, columnPolicy);
            MeasurementServer server = new MeasurementServer(table, 10 * LineitemAttributes.maxStringLength);
            setUnaryTranslationMeta(server);

            // add secret shares for all user groups
            for (int i = 1; i <= numGroups; i++) {
                List<Integer> translatedGroupNumber = UnaryTranslator.getUnaryTranslatorSingleton().translatePositiveIntegerString("lineitem", String.valueOf(i));
                List<List<Integer>> groupNumberSecretShares = SecretCreator.getSecretCreatorSingleton().createSecrets(translatedGroupNumber, 1);
                server.addSharesForCredential(String.valueOf(i), groupNumberSecretShares.get(0));
            }

            setUnaryTranslationMeta(server);

            for (int numConditions : queryConditions) {
                long time = server.sendCountQueryAndCredentials(QueryCreator.getCountQuery(numConditions, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite COUNT query (numConditions=" + numConditions + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);

                time = server.sendSumQueryAndCredentials(QueryCreator.getSumQuery(numConditions, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite SUM query (numConditions=" + numConditions + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);

                time = server.sendCountQueryAndCredentials(QueryCreator.getCountQuery(numConditions, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                time += server.sendSumQueryAndCredentials(QueryCreator.getSumQuery(numConditions, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite AVG query (numConditions=" + numConditions + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);

                System.out.println();
            }
        }
    }

    private void setUnaryTranslationMeta(MeasurementServer server) {
        HashMap<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put(LineitemAttributes.tableName, LineitemAttributes.maxStringLength);
        server.sendUnaryTranslationMeta(unaryTranslationMeta);
    }

}
