package owner.measurements;

import common.UnaryTranslator;
import owner.lineitem.LineitemAttributes;
import owner.lineitem.LineitemReader;

import java.util.*;

public class MeasurementLineitemUploader {

    /**
     * @return time in nanoseconds to create the tables
     */
    public static long uploadTables(int batchSize, int rowLimit, int columnLimit, boolean columnPolicy, int numGroups, int accessibilityPercentage,
                                    int numServers, boolean attachPolicy, boolean storeToFile) throws Exception {
        LineitemReader lineitemReader = new LineitemReader();
        String[] attributeNames = LineitemAttributes.getAttributes(attachPolicy, columnPolicy, columnLimit);
        int numColumns = attributeNames.length;
        UnaryTranslator unaryTranslator = UnaryTranslator.getUnaryTranslatorSingleton();
        unaryTranslator.getUnaryTranslationMeta().put(LineitemAttributes.tableName, LineitemAttributes.maxStringLength);

        List<String[]> batch;

        long time = 0;
        int progress = 0;

        while ((batch = lineitemReader.nextBatch(batchSize, rowLimit)) != null) {
            Map<String, List<Integer>> table = new HashMap<>();
            for (String attribute : attributeNames) {
                table.put(attribute, new ArrayList<>());
            }

            long startTime = System.nanoTime();
            for (String[] row : batch) {
                for (int i = 0; i < numColumns; i++) {
                    if (i < columnLimit) {
                        Class<?> attributeType = LineitemAttributes.attributeTypes.get(LineitemAttributes.attributeNames[i]);
                        if (attributeType == Integer.class) {
                            table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveIntegerString(LineitemAttributes.tableName, row[i]));
                        } else if (attributeType == Float.class) {
                            table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveFloatString(LineitemAttributes.tableName, row[i]));
                        } else {
                            throw new Exception("Unknown type " + attributeType.getName());
                        }
                    } else if (attachPolicy) {
                        int groupNumber = generateGroupNumber(accessibilityPercentage, numGroups);
                        table.get(attributeNames[i]).addAll(unaryTranslator.translatePositiveIntegerString(LineitemAttributes.tableName, String.valueOf(groupNumber)));
                    }
                }
            }

            MeasurementDataDistributor.distributeData(table, numServers, storeToFile, progress == 0);

            time += System.nanoTime() - startTime;
            progress += batchSize;
        }
        return time;
    }

    private static int generateGroupNumber(int accessibilityPercentage, int numGroups) {
        Random r = new Random();
        int randomNum = r.nextInt(100);
        if (randomNum < accessibilityPercentage) {
            return 1;
        } else {
            return r.nextInt(numGroups-1) + 2;
        }
    }

}
