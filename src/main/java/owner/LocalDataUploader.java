package owner;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

public class LocalDataUploader {

    public static void clearTable(List<String> attributeNames, String tableName, Connection con) throws SQLException {
        Statement deleteStatement = con.createStatement();
        deleteStatement.executeUpdate("DROP TABLE IF EXISTS " + tableName + ";");

        Statement createStatement = con.createStatement();
        StringBuilder createQueryString = new StringBuilder("CREATE TABLE "+ tableName + "(");
        for (int i = 0; i < attributeNames.size(); i++) {
            if (i < attributeNames.size()-1) createQueryString.append(attributeNames.get(i)).append(" integer, ");
            else createQueryString.append(attributeNames.get(i)).append(" integer);");
        }
        createStatement.executeUpdate(createQueryString.toString());
    }

    public static void insertBatch(Map<String, List<Integer>> table, String tableName, Connection con) throws SQLException {
        con.setAutoCommit(false); // commit whole batch at the end
        List<String> attributeNames = table.keySet().stream().toList();
        StringBuilder insertQueryString = new StringBuilder("INSERT INTO " + tableName + " VALUES (");
        for (int i = 0; i < attributeNames.size(); i++) {
            insertQueryString.append("?");
            if (i < attributeNames.size()-1) insertQueryString.append(", ");
            else insertQueryString.append(")");
        }

        PreparedStatement insertStatement = con.prepareStatement(insertQueryString.toString());

        for (int i = 0; i < table.get(attributeNames.get(0)).size(); i++) {
            for (int j = 0; j < attributeNames.size(); j++) {
                insertStatement.setInt(j+1, table.get(attributeNames.get(j)).get(i));
            }
            insertStatement.addBatch();
        }

        insertStatement.executeBatch();
        con.commit();
    }

    public static void uploadLineItem(Map<String, List<Integer>> table, Connection con, String[] attributeNames, String[] primaryKey) throws SQLException {
        con.setAutoCommit(false); // commit whole batch at the end

        Statement deleteStatement, createStatement;

        createStatement = con.createStatement();
        deleteStatement = con.createStatement();
        deleteStatement.executeUpdate("DROP TABLE IF EXISTS LineItem;");

        PreparedStatement insertStatement;

        StringBuilder createQueryString = new StringBuilder("CREATE TABLE LineItem(");
        for (String name : attributeNames) {
            createQueryString.append(name).append(" integer, ");
        }
        // example: "CREATE TABLE LineItem(orderkey integer, partkey integer, suppkey integer, linenumber integer, tupleP integer,"

        createQueryString.append("PRIMARY KEY (");
        for (int i = 0; i < primaryKey.length; i++) {
            createQueryString.append(primaryKey[i]);
            if (i < primaryKey.length-1) createQueryString.append(",");
            else createQueryString.append("))");
        }
        // example: "CREATE TABLE LineItem(orderkey integer, partkey integer, suppkey integer, linenumber integer, tuplep integer, PRIMARY KEY (orderkey, linenumber))"

        createStatement.executeUpdate(createQueryString.toString());

        StringBuilder insertQueryString = new StringBuilder("INSERT INTO LineItem VALUES (");
        for (int i = 0; i < attributeNames.length; i++) {
            insertQueryString.append("?");
            if (i < attributeNames.length-1) insertQueryString.append(", ");
            else insertQueryString.append(")");
        }

        insertStatement = con.prepareStatement(insertQueryString.toString());

        for (int i = 0; i < table.get(attributeNames[0]).size(); i++) {
            for (int j = 0; j < attributeNames.length; j++) {
                insertStatement.setInt(j+1, table.get(attributeNames[j]).get(i));
            }
            insertStatement.addBatch();
        }

        insertStatement.executeBatch();
        con.commit();
    }

    public static void storeToFile(Map<String, List<Integer>> table, String filePath, boolean initialize) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, !initialize));
        List<String> attributeNames = table.keySet().stream().toList();

        for (int i = 0; i < table.get(attributeNames.get(0)).size(); i++) {
            for (int j = 0; j < attributeNames.size(); j++) {
                writer.write(table.get(attributeNames.get(j)).get(i).toString());
                if (j < attributeNames.size()-1) writer.write("|");
                else writer.write("\n");
            }
        }

        writer.flush();
        writer.close();
    }

}
