package common;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BatchedReader {

    protected BufferedReader reader;
    protected int rowCounter = 0;

    public BatchedReader(String file) {
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.err.println("\nCould not open file: " + e.getMessage());
            System.exit(0);
        }
    }

    /**
     * Reads the next 'batchSize' rows of the lineitem.tbl file (or until the end of the file, or until the 'rowLimit'
     * is reached) and returns them as a list.
     *
     * @return the next batch of rows of the lineitem.tbl file if there are any remaining rows. {@code null} if there
     * are no remaining rows to return.
     * @throws IOException if the {@link BufferedReader} failed.
     */
    public List<String[]> nextBatch(int batchSize, int rowLimit) throws IOException {
        List<String[]> batch = new ArrayList<>();
        String[] values;
        int batchCounter = 0;
        while (batchCounter < batchSize) {
            values = nextRow(rowLimit);
            if (values == null && batchCounter == 0) return null;
            else if (values == null) break;
            batch.add(values);
            batchCounter++;
        }
        return batch;
    }

    public String[] nextRow(int rowLimit) throws IOException {
        String line;
        if ((line = reader.readLine()) != null && rowCounter < rowLimit && !line.isBlank()) {
            rowCounter++;
            return line.split("\\|");
        } else {
            return null;
        }
    }

}
