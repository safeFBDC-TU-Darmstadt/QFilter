package server.measurements;

import user.exceptions.QueryProcessingException;
import common.Query;
import common.TranslatedQueryCondition;
import server.BatchedFileTable;
import server.Server;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public class MeasurementServer extends Server {

    public MeasurementServer(BatchedFileTable batchedFileTable, int sharesPerValue) {
        this.batchedFileTable = batchedFileTable;
        this.sharesPerValue = sharesPerValue;
    }

    /**
     * @return time in nanoseconds to rewrite the query
     */
    protected long timeToRewriteTheQuery(Query query, List<String> credentials) {
        long startTime = System.nanoTime();
        super.rewriteTheQuery(query, credentials);
        return System.nanoTime() - startTime;
    }

    /**
     * @return time in nanoseconds to process the query
     */
    public long sendCountQueryAndCredentials(Query query, List<String> credentials, boolean processQuery) throws RemoteException, QueryProcessingException {
        if (!processQuery) {
            return timeToRewriteTheQuery(query, credentials);
        }

        List<List<TranslatedQueryCondition>> policyConditions = rewriteTheQuery(query, credentials);

        BigInteger result = new BigInteger("0");

        Map<String, List<Integer>> nextTupleBatch;
        boolean resetReader = true;
        long time = 0;
        while ((nextTupleBatch = batchedFileTable.nextTupleBatch(batchSize*sharesPerValue, resetReader)) != null) {
            long start = System.nanoTime();
            List<BigInteger> resultOfSMP = getStringMatchingResults(query, policyConditions, nextTupleBatch);
            for (BigInteger bigInteger : resultOfSMP) {
                result = result.add(bigInteger);
            }

            time += System.nanoTime() - start;

            if (resetReader) resetReader = false;
        }

        return time;
    }

    /**
     * @return time in nanoseconds to process the query
     */
    public long sendSumQueryAndCredentials(Query query, List<String> credentials, boolean processQuery) throws RemoteException, QueryProcessingException {
        if (!processQuery) {
            return timeToRewriteTheQuery(query, credentials);
        }

        List<List<TranslatedQueryCondition>> policyConditions = rewriteTheQuery(query, credentials);

        final int symbolsPerValue = unaryTranslationMeta.get(query.getTable());
        final int sharesPerSymbol = sharesPerValue / symbolsPerValue;

        BigInteger[][] result = new BigInteger[symbolsPerValue][sharesPerSymbol];

        // initialize result array with zeros
        for (int i = 0; i < symbolsPerValue; i++) {
            for (int j = 0; j < sharesPerSymbol; j++) {
                result[i][j] = new BigInteger("0");
            }
        }

        Map<String, List<Integer>> nextTupleBatch;
        boolean resetReader = true;
        long time = 0;
        while ((nextTupleBatch = batchedFileTable.nextTupleBatch(batchSize*sharesPerValue, resetReader)) != null) {
            long start = System.nanoTime();

            List<BigInteger> resultOfSMP = null;

            // noConditions will only be true if there is no policy attached and there are no query conditions;
            // no need for string-matching in that case
            boolean noConditions = query.getConditions().isEmpty() && policyConditions.isEmpty();
            if (!noConditions)
                resultOfSMP = getStringMatchingResults(query, policyConditions, nextTupleBatch);
            List<Integer> valuesOfAttribute = nextTupleBatch.get(query.getAttribute());

            // Individually add up products of unary values of the 'sum-attribute' (or rather their shares) and the result
            // of the string matching operation. If we consider the original unary translations, this will result in a list
            // (for each digit of the result!) that determines which digits were added up at this position in the result string.
            for (int i = 0; i < valuesOfAttribute.size() / sharesPerValue; i++) {
                for (int j = 0; j < symbolsPerValue; j++) {
                    for (int k = 0; k < sharesPerSymbol; k++) {
                        BigInteger attributeValue = new BigInteger(String.valueOf(valuesOfAttribute.get(i * sharesPerValue + j * sharesPerSymbol + k)));
                        if (noConditions) result[j][k] = result[j][k].add(attributeValue);
                        else result[j][k] = result[j][k].add(resultOfSMP.get(i).multiply(attributeValue));
                    }
                }
            }

            time += System.nanoTime() - start;

            if (resetReader) resetReader = false;
        }

        return time;
    }

}
