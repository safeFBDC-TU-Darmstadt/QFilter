import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import owner.measurements.MeasurementLineitemUploader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

// Measures the time to generate secret shares of the lineitem table (only using the first four columns) for a variable
// number of user groups, as well as the combined file sizes for the tables (in Byte).
public class Experiment4 {

    int[] getUserGroups() {
        return new int[]{2, 10, 20};
    }

    int numServers = 15;
    int numColumns = 4;
    int numRows = 100_000;
    int accessibilityPercentage = 50;

    int batchSize = 10_000;


    @BeforeEach
    void init() {
        UnaryTranslator.initUnaryTranslatorSingleton(new HashMap<>());
        SecretCreator.initSecretCreatorSingleton(1);
    }

    @Test
    void measureTimes() throws Exception {
        // no policy attachment
        for (int numGroups : getUserGroups()) {
            long time = MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, false, numGroups, accessibilityPercentage, numServers, false, false);
            System.out.println("Time (ns) for upload (attachPolicy:" + false + ", columnLimit:" + numColumns + ", rowLimit:" + numRows
                    + ", numGroups:" + numGroups + ", accessibilityPercentage:" + accessibilityPercentage + ", numServers:" + numServers + "): " + time);
        }

        // tuple & column level policy
        for (boolean columnPolicy : new boolean[]{false, true}) {
            for (int numGroups : getUserGroups()) {
                long time = MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, columnPolicy, numGroups, accessibilityPercentage, numServers, true, false);
                System.out.println("Time (ns) for upload (attachPolicy:" + true + ", columnPolicy:" + columnPolicy + ", columnLimit:" + numColumns + ", rowLimit:" + numRows
                        + ", numGroups:" + numGroups + ", accessibilityPercentage:" + accessibilityPercentage + ", numServers:" + numServers + "): " + time);
            }
        }
    }

    @Test
    void measureStorage() throws Exception {
        // no policy attachment
        for (int numGroups : getUserGroups()) {
            MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, false, numGroups, accessibilityPercentage, numServers, false, true);
            System.out.println("Storage size (B) (attachPolicy:" + false + ", columnLimit:" + numColumns + ", rowLimit:" + numRows
                    + ", numGroups:" + numGroups + ", accessibilityPercentage:" + accessibilityPercentage + ", numServers:" + numServers + "): " + determineStorageSize(numServers));
        }

        // tuple & column level policy
        for (boolean columnPolicy : new boolean[]{false, true}) {
            for (int numGroups : getUserGroups()) {
                MeasurementLineitemUploader.uploadTables(batchSize, numRows, numColumns, columnPolicy, numGroups, accessibilityPercentage, numServers, true, true);
                System.out.println("Storage size (B) for upload (attachPolicy:" + true + ", columnPolicy:" + columnPolicy + ", columnLimit:" + numColumns + ", rowLimit:" + numRows
                        + ", numGroups:" + numGroups + ", accessibilityPercentage:" + accessibilityPercentage + ", numServers:" + numServers + "): " + determineStorageSize(numServers));
            }
        }

        deleteFiles(15);
    }

    long determineStorageSize(int numServers) throws IOException {
        long size = 0;
        for (int i = 1; i <= numServers; i++)
            size += Files.size(Path.of("src/main/resources/lineitem-" + i + ".tbl"));
        return size;
    }

    void deleteFiles(int numServers) throws IOException {
        for (int i = 1; i <= numServers; i++)
            Files.delete(Path.of("src/main/resources/lineitem-" + i + ".tbl"));
    }

}