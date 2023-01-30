import common.*;
import org.junit.jupiter.api.Test;
import owner.lineitem.LineitemAttributes;
import owner.measurements.MeasurementLineitemUploader;
import server.BatchedFileTable;
import server.measurements.MeasurementServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Measures the time to process queries (different numbers of conditions, different aggregate functions, different conditional
// types (AND, OR)) for a variable number of columns of the lineitem table.
public class Experiment9 extends ServerExperiment {

    int[] numAttributes = new int[]{3, 5, 7};
    int numRows = 100_000;
    int numGroups = 2;
    int accessibilityPercentage = 50;
    int numServers = 1;

    int batchSize = 10_000;
    List<Query> queries;

    public Experiment9() throws Exception {
        super();
        resetQueries(numAttributes[0]);
    }

    void resetQueries(int numColumns) throws Exception {
        queries = new ArrayList<>();
        for (ConditionalType c : ConditionalType.values()) {
            for (AggregateType t : AggregateType.values()) {
                for (int numConditions : new int[]{0, 2, 4}) {
                    if (numConditions <= numColumns) {
                        queries.add(QueryCreator.getQuery(t, c, numConditions));
                    }
                }
            }
        }
    }

    @Test
    void measureTime() throws Exception {
        for (int numColumns : numAttributes) {
            // create lineitem table with specific attributes (w/o policy attachment)
            MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, false, numGroups, accessibilityPercentage, numServers, false, true);

            BatchedFileTable table = new BatchedFileTable(Arrays.stream(LineitemAttributes.getAttributes(false, false, numColumns)).toList(), "src/main/resources/lineitem-1.tbl", false, false);
            MeasurementServer server = new MeasurementServer(table, 10 * LineitemAttributes.maxStringLength);

            setUnaryTranslationMeta(server);
            resetQueries(numColumns);

            for (Query query : queries) {
                int numConditions = query.getConditions().size();
                long time = getProcessingTime(server, query);
                System.out.println("Time (ns) to process " + query.getAggregateType().token.toUpperCase() + " query (attachPolicy=" + false + ", numConditions=" + numConditions + ", columnLimit=" + numColumns + ", rowLimit=" + numRows + "): " + time);
            }
        }

        for (boolean columnPolicy : new boolean[]{false, true}) {
            for (int numColumns : numAttributes) {
                // create lineitem table with specific attributes (w/ policy attachment)
                MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, columnPolicy, numGroups, accessibilityPercentage, numServers, true, true);

                BatchedFileTable table = new BatchedFileTable(Arrays.stream(LineitemAttributes.getAttributes(true, columnPolicy, numColumns)).toList(), "src/main/resources/lineitem-1.tbl", true, columnPolicy);
                MeasurementServer server = new MeasurementServer(table, 10 * LineitemAttributes.maxStringLength);

                setUnaryTranslationMeta(server);

                // add secret shares for all user groups
                for (int i = 1; i <= numGroups; i++) {
                    List<Integer> translatedGroupNumber = UnaryTranslator.getUnaryTranslatorSingleton().translatePositiveIntegerString("lineitem", String.valueOf(i));
                    List<List<Integer>> groupNumberSecretShares = SecretCreator.getSecretCreatorSingleton().createSecrets(translatedGroupNumber, 1);
                    server.addSharesForCredential(String.valueOf(i), groupNumberSecretShares.get(0));
                }

                setUnaryTranslationMeta(server);
                resetQueries(numColumns);

                for (Query query : queries) {
                    int numConditions = query.getConditions().size();
                    long time = getProcessingTime(server, query);
                    System.out.println("Time (ns) to process " + query.getAggregateType().token.toUpperCase() + " query (attachPolicy:" + true + ", columnPolicy:" + columnPolicy + ", numConditions=" + numConditions
                            + ", columnLimit:" + numColumns + ", rowLimit:" + numRows + ", numGroups:" + numGroups + ", accessibilityPercentage:" + accessibilityPercentage + ", numServers:" + numServers + "): " + time);
                }
            }
        }
    }

}
