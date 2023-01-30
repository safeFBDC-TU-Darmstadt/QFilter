import common.Query;
import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.Test;
import owner.lineitem.LineitemAttributes;
import owner.measurements.MeasurementLineitemUploader;
import server.BatchedFileTable;
import server.measurements.MeasurementServer;

import java.util.Arrays;
import java.util.List;

// Measures the time to process a queries (different numbers of conditions, different aggregate functions, different conditional
// types (AND, OR)) on the lineitem table (only the first four columns are used) for a variable number of user groups.
public class Experiment12 extends ServerExperiment {

    int[] groups = new int[]{2,20,40};
    int numServers = 1;
    int numColumns = 4;
    int numRows = 100_000;
    int accessibilityPercentage = 50;

    int batchSize = 10_000;
    List<Query> queries = QueryCreator.getAllQueries();

    public Experiment12() throws Exception {
        super();
    }

    void resetQueries() throws Exception {
        queries = QueryCreator.getAllQueries();
    }

    @Test
    void measureTime() throws Exception {
        for (int numGroups : groups) {
            // create lineitem table with specific attributes (w/o policy attachment)
            MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, false, numGroups, accessibilityPercentage, numServers, false, true);
            BatchedFileTable table = new BatchedFileTable(Arrays.stream(LineitemAttributes.getAttributes(false, false, 4)).toList(), "src/main/resources/lineitem-1.tbl", false, false);
            MeasurementServer server = new MeasurementServer(table, 10 * LineitemAttributes.maxStringLength);

            setUnaryTranslationMeta(server);
            resetQueries();

            for (Query query : queries) {
                int numConditions = query.getConditions().size();
                long time = getProcessingTime(server, query);
                System.out.println("Time (ns) to process " + query.getAggregateType().token.toUpperCase() + " query (attachPolicy=" + false + ", numConditions=" + numConditions + ", columnLimit=" + numColumns + ", rowLimit=" + numRows + "): " + time);
            }
        }

        for (boolean columnPolicy : new boolean[]{false, true}) {
            BatchedFileTable table = new BatchedFileTable(Arrays.stream(LineitemAttributes.getAttributes(true, columnPolicy, 4)).toList(), "src/main/resources/lineitem-1.tbl", true, columnPolicy);
            MeasurementServer server = new MeasurementServer(table, 10 * LineitemAttributes.maxStringLength);
            setUnaryTranslationMeta(server);

            for (int numGroups : groups) {
                // create lineitem table with specific attributes (w/ policy attachment)
                MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, columnPolicy, numGroups, accessibilityPercentage, numServers, true, true);

                // add secret shares for all user groups
                for (int i = 1; i <= numGroups; i++) {
                    List<Integer> translatedGroupNumber = UnaryTranslator.getUnaryTranslatorSingleton().translatePositiveIntegerString("lineitem", String.valueOf(i));
                    List<List<Integer>> groupNumberSecretShares = SecretCreator.getSecretCreatorSingleton().createSecrets(translatedGroupNumber, 1);
                    server.addSharesForCredential(String.valueOf(i), groupNumberSecretShares.get(0));
                }

                resetQueries();

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