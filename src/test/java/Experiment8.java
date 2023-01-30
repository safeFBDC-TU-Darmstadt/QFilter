import common.ConditionalType;
import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import owner.lineitem.LineitemAttributes;
import server.BatchedFileTable;
import server.measurements.MeasurementServer;

import java.util.*;

// Measures the time to rewrite a query with two conditions (always combined by AND) and different aggregation functions
// for a variable number of user groups.
public class Experiment8 {

    int[] groups = new int[]{2,2,20,40}; // first configuration is run twice: first time to set up; only the
                                            // second result is taken for stable outcomes (exclude caching, ...)
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

            for (int numGroups : groups) {
                // add secret shares for all user groups
                for (int i = 1; i <= numGroups; i++) {
                    List<Integer> translatedGroupNumber = UnaryTranslator.getUnaryTranslatorSingleton().translatePositiveIntegerString("lineitem", String.valueOf(i));
                    List<List<Integer>> groupNumberSecretShares = SecretCreator.getSecretCreatorSingleton().createSecrets(translatedGroupNumber, 1);
                    server.addSharesForCredential(String.valueOf(i), groupNumberSecretShares.get(0));
                }

                setUnaryTranslationMeta(server);

                long time = server.sendCountQueryAndCredentials(QueryCreator.getCountQuery(2, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite COUNT query (numConditions=" + 2 + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);

                time = server.sendSumQueryAndCredentials(QueryCreator.getSumQuery(2, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite SUM query (numConditions=" + 2 + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);

                time = server.sendCountQueryAndCredentials(QueryCreator.getCountQuery(2, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                time += server.sendSumQueryAndCredentials(QueryCreator.getSumQuery(2, ConditionalType.CONJUNCTIVE), Collections.singletonList("1"), false);
                System.out.println("Time (ns) to rewrite AVG query (numConditions=" + 2 + ", rowLimit=" + numRows + ", numGroups=" + numGroups + ", columnLimit=" + numColumns
                        + ", accessibilityPercentage=" + accessibilityPercentage + ", attachPolicy=" + true + ", columnPolicy=" + columnPolicy + "): " + time);
            }
        }
    }

    private void setUnaryTranslationMeta(MeasurementServer server) {
        HashMap<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put(LineitemAttributes.tableName, LineitemAttributes.maxStringLength);
        server.sendUnaryTranslationMeta(unaryTranslationMeta);
    }

}
