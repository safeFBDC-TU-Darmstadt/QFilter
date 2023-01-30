package common;

import java.util.*;

public class SecretCreator {

    private final int polyDegree;
    private static SecretCreator secretCreatorSingleton;

    private SecretCreator(int polyDegree) {
        this.polyDegree = polyDegree;
    }

    public static SecretCreator initSecretCreatorSingleton(int polyDegree) {
        if (secretCreatorSingleton == null)
            secretCreatorSingleton = new SecretCreator(polyDegree);
        return secretCreatorSingleton;
    }

    /**
     * Creates random coefficients for a list of size 'numRows' of values for the secret sharing process using a linear polynomial.
     * This method will be used to create coefficients for data splitting. For secret sharing, the same coefficients need to be used for all servers.
     * Creating all secrets at the same time, however, uses up too much memory. Thus, we need this method to make the memory usage of data splitting more predictable.
     *
     * @param numRows The number of values to create coefficients for.
     * @return A list of size 'numRows' containing random coefficients (usually for a given list of binary values after the translation of the values of an attribute).
     */
    public List<Integer> createCoefficients(int numRows) {
        List<Integer> coefficients = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < numRows; i++) {
            coefficients.add(r.nextInt(10) + 1);
        }
        return coefficients;
    }

    /**
     * Creates secrets of given binary values for a given number of servers. This method will mainly be used for creating
     * secret shares of a condition in a query for multiple servers.
     *
     * @param binaryValues A list of binary values.
     * @param numServers   the number of servers to create secrets for.
     * @return lists of secret shares for a given number of servers.
     */
    public List<List<Integer>> createSecrets(List<Integer> binaryValues, int numServers) {
        List<List<Integer>> secrets = new ArrayList<>();
        List<Integer> coefficients = createCoefficients(binaryValues.size());
        for (int i = 0; i < numServers; i++) {
            secrets.add(new ArrayList<>());
        }

        for (int i = 0; i < numServers; i++) {
            for (int j = 0; j < binaryValues.size(); j++) {
                secrets.get(i).add(randomPolynomial(binaryValues.get(j), i + 1, coefficients.get(j)));
            }
        }
        return secrets;
    }

    public int randomPolynomial(int value, int x, int coeff) {
        return value + x * coeff;
    }

    public static SecretCreator getSecretCreatorSingleton() {
        return secretCreatorSingleton;
    }
}
