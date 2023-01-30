package owner.measurements;

import common.SecretCreator;
import owner.DataSplitter;
import owner.LocalDataUploader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeasurementDataDistributor {

    public static void distributeData(Map<String, List<Integer>> translatedTable, int numServers, boolean storeToFile, boolean initialize) throws IOException {
        // all attributes have the same number of rows
        int rows = translatedTable.values().stream().toList().get(0).size();
        List<List<Integer>> coefficients = new ArrayList<>();
        for (int i = 0; i < translatedTable.size(); i++) {
            coefficients.add(SecretCreator.getSecretCreatorSingleton().createCoefficients(rows));
        }

        for (int i = 0; i < numServers; i++) {
            Map<String, List<Integer>> secretTable = DataSplitter.createSecrets(translatedTable, i + 1, coefficients);
            if (storeToFile) LocalDataUploader.storeToFile(secretTable, "src/main/resources/lineitem-" + (i + 1) + ".tbl", initialize);
        }
    }

}
