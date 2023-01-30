package owner.lineitem;

import common.IServer;
import common.UnaryTranslator;
import owner.DataDistributor;

import java.util.*;

public class LineitemUploader {

    /**
     * Uploads the 'lineitem' table to a given list of servers after performing unary translations and creating secret shares of said translations.
     *
     * @param batchSize               Limits the number of rows that will be processes and uploaded at each time.
     * @param rowLimit                Limits the total number of rows of the original table that will be uploaded to servers.
     * @param columnPolicy            {@code true} if a column level policy should be attached.
     *                                {@code false} if a tuple level policy should be attached.
     * @param numGroups The number of user groups to use for the give policy strategy.
     * @param accessibilityPercentage The accessibility percentage for group 1.
     * @param serverStubs The list of servers to upload the 'lineitem' table to.
     * @throws Exception if an unhandled (new) attribute is encountered, a remote method call failed or the {@link LineitemReader} failed.
     */
    public static void uploadTables(int batchSize, int rowLimit, boolean columnPolicy, int numGroups,
                                    int accessibilityPercentage, List<IServer> serverStubs) throws Exception {
        LineitemReader lineitemReader = new LineitemReader();
        String[] attributeNames = LineitemAttributes.getAttributes(true, columnPolicy, LineitemAttributes.attributeNames.length);
        int numColumns = attributeNames.length;
        UnaryTranslator unaryTranslator = UnaryTranslator.getUnaryTranslatorSingleton();
        unaryTranslator.getUnaryTranslationMeta().put(LineitemAttributes.tableName, LineitemAttributes.maxStringLength);

        List<String[]> batch;
        boolean initializeOnServers = true; // tells the servers whether this is the first batch
        while ((batch = lineitemReader.nextBatch(batchSize, rowLimit)) != null) { // read in and send data batch-wise
            Map<String, List<Integer>> table = new HashMap<>();
            // prepare resulting table (create new list for each attribute)
            for (String attribute : attributeNames) {
                table.put(attribute, new ArrayList<>());
            }

            for (String[] row : batch) {
                for (int i = 0; i < numColumns; i++) {
                    // perform the unary translation directly on the read-in data (could be extracted to a different method)
                    if (i < LineitemAttributes.attributeNames.length) { // all attributes will be uploaded; the original attribute names are
                                                                        // stored first in 'attributeNames', then come the policy attribute names
                        Class<?> attributeType = LineitemAttributes.attributeTypes.get(attributeNames[i]);
                        if (attributeType == Integer.class) {
                            table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveIntegerString(LineitemAttributes.tableName, row[i]));
                        } else if (attributeType == Float.class) {
                            table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveFloatString(LineitemAttributes.tableName, row[i]));
                        } else {
                            throw new Exception("Unknown type " + attributeType.getName());
                        }
                    } else {
                        int groupNumber = generateGroupNumber(accessibilityPercentage, numGroups);
                        table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveIntegerString(LineitemAttributes.tableName, String.valueOf(groupNumber)));
                    }
                }
            }

            DataDistributor.distributeData(LineitemAttributes.tableName, table, serverStubs, initializeOnServers,LineitemAttributes.maxStringLength * 10, columnPolicy);
            if (initializeOnServers) initializeOnServers = false;
        }
    }

    /**
     * Creates a random group number according to the accessibility percentage of group 1 ('accessibilityPercentage') and
     * the rest of the groups (1-'accessibilityPercentage').
     */
    private static int generateGroupNumber(int accessibilityPercentage, int numGroups) {
        Random r = new Random();
        int randomNum = r.nextInt(100);
        if (randomNum < accessibilityPercentage) {
            return 1;
        } else {
            if (numGroups == 2) return 2;
            return r.nextInt(numGroups-1) + 2;
        }
    }

}
