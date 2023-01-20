package owner;

import common.IServer;
import common.SecretCreator;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DataDistributor {

    /**
     * Creates secret shares for the given translated table and each server of serverStubs. Proceeds to send these tables
     * of secret shares to the servers.
     *
     * @param tableName       The name of the table to send.
     * @param translatedTable A map containing the values of all attributes after the unary translation. The keys are
     *                        given by the attribute names, the values are lists of binary values.
     * @param serverStubs     The list of servers to send secret shares to.
     * @throws RemoteException If a remote method call failed.
     */
    public static void distributeData(String tableName, Map<String, List<Integer>> translatedTable, List<IServer> serverStubs, boolean initialize, int sharesPerValue, boolean columnPolicy) throws RemoteException {
        int numServers = serverStubs.size();

        // all attributes have the same number of rows
        int rows = translatedTable.values().stream().collect(Collectors.toList()).get(0).size();

        // create a coefficient for a linear polynomial for each value in the translated table
        List<List<Integer>> coefficients = new ArrayList<>();
        for (int i = 0; i < translatedTable.size(); i++) {
            coefficients.add(SecretCreator.getSecretCreatorSingleton().createCoefficients(rows));
        }

        // create tables of secret shares for all servers; send each table to the corresponding server
        for (int i = 0; i < numServers; i++) {
            Map<String, List<Integer>> secretTable = DataSplitter.createSecrets(translatedTable, i + 1, coefficients);
            serverStubs.get(i).sendTable(tableName, secretTable, initialize, sharesPerValue, i + 1, columnPolicy);
        }
    }

}
