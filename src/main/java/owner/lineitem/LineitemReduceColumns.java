package owner.lineitem;

import java.io.*;

/*
Class to generate the lineitem.tbl file with a reduced amount of columns
 */
public class LineitemReduceColumns {

    public static void main(String[] args) {
        // original lineitem table file has to be copied to this path!
        String pathToOriginalFile = "src/main/resources/lineitem-original.tbl";
        int numColumns = 8;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(pathToOriginalFile));
            BufferedWriter writer= new BufferedWriter(new FileWriter(LineitemAttributes.filePath));

            String line = reader.readLine();
            while (line != null) {
                String[] values = line.split("\\|");

                for (int i = 0; i < numColumns-1; i++) {
                    writer.write(values[i] + "|");
                }

                line = reader.readLine();
                writer.write(values[numColumns-1]);
                if (line != null) writer.write("\n");
            }

            writer.flush();

        } catch (FileNotFoundException e) {
            System.out.println("\nCould not open file: " + e.getMessage());
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
