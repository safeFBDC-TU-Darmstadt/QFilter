package owner;

import common.SecretCreator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSplitter {

    /**
     * Creates a table of secret shares using the given coefficients for a polynomial of degree one.
     * A secret share of value b, coefficient c and server number x will be given by f(b,c,x) = b + c*x.
     *
     * @param translatedTable A map containing the values of all attributes after the unary translation.
     *                        The keys are given by the attribute names, the values are lists of binary values.
     * @param serverNumber The server number to create the secret shares for.
     * @param coefficients The coefficients to create the secret shares with. The outer list has to be of length l1
     *                     where l1 is the number of attributes in the given map (table). Each inner list has to be of
     *                     length l2 where l2 is the number of values for each attribute in the given map (table).
     * @return A map containing the secret shares of all original values in the given map (same structure as above).
     */
    public static Map<String, List<Integer>> createSecrets(Map<String, List<Integer>> translatedTable, int serverNumber, List<List<Integer>> coefficients) {
        SecretCreator secretCreator = SecretCreator.getSecretCreatorSingleton();

        Map<String, List<Integer>> secretTable = new HashMap<>();

        for (String key : translatedTable.keySet())
            secretTable.put(key, new ArrayList<>());

        var entries = translatedTable.entrySet().stream().toList();
        for (int i = 0; i < translatedTable.keySet().size(); i++) {
            var entry = entries.get(i);
            for (int j = 0; j < entry.getValue().size(); j++) {
                secretTable.get(entry.getKey()).add(secretCreator.randomPolynomial(entry.getValue().get(j), serverNumber, coefficients.get(i).get(j)));
            }
        }

        return secretTable;
    }

}
