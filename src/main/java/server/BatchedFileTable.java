package server;

import common.BatchedReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BatchedFileTable {

    // The list of attribute names of the attribute table (to store the order)
    private final List<String> attributeNames;
    private BatchedReader batchedReader = null;
    private final String filePath;
    private boolean policyAttached;
    private boolean columnPolicy;

    public BatchedFileTable(List<String> attributeNames, String file) {
        this.attributeNames = attributeNames;
        this.filePath = file;
    }

    public BatchedFileTable(List<String> attributeNames, String file, boolean policyAttached, boolean columnPolicy) {
        this.attributeNames = attributeNames;
        this.filePath = file;
        this.policyAttached = policyAttached;
        this.columnPolicy = columnPolicy;
    }


    public Map<String, List<Integer>> nextTupleBatch(int batchSize, boolean resetReader) {
        if (resetReader) {
            this.batchedReader = new BatchedReader(filePath);
        }

        List<String[]> nextRows = null;

        try {
            nextRows = batchedReader.nextBatch(batchSize, Integer.MAX_VALUE);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        if (nextRows == null) return null;

        Map<String, List<Integer>> nextTupleBatch = new HashMap<>();

        for (String attributeName : attributeNames)
            nextTupleBatch.put(attributeName, new ArrayList<>());

        for (String[] row : nextRows) {
            for (int i = 0; i < row.length; i++) {
                nextTupleBatch.get(attributeNames.get(i)).add(Integer.valueOf(row[i]));
            }
        }

        return nextTupleBatch;
    }

    public void appendAttributeTable(Map<String, List<Integer>> nextTupleBatch, boolean initialize) throws IOException {
        int numRows = nextTupleBatch.values().stream().toList().get(0).size();
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, !initialize));
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < attributeNames.size(); j++) {
                writer.write(String.valueOf(nextTupleBatch.get(attributeNames.get(j)).get(i)));
                if (j < attributeNames.size()-1) writer.write("|");
                else writer.write("\n");
            }
            writer.flush();
        }
        writer.close();
    }

    public boolean isPolicyAttached() {
        return policyAttached;
    }

    public boolean isColumnPolicy() {
        return columnPolicy;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }
}
