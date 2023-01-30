package server;

import manual.Configuration;
import manual.Starter;
import user.exceptions.QueryProcessingException;
import common.Query;
import common.TranslatedQueryCondition;
import common.AggregateType;
import common.ConditionalType;
import common.IServer;

import java.io.IOException;
import java.math.BigInteger;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Server implements IServer {

	protected BatchedFileTable batchedFileTable;
	protected final int batchSize = 100;
	protected int sharesPerValue;
	// The table with credentials and policy values in them
	protected Map<String, List<Integer>> policyOfCredsTable = new HashMap<>();

	protected Map<String, Integer> unaryTranslationMeta;
	protected int nrOfFiguresProValue = 0;
	protected Server(){
	}

	public static void main (String[] args) throws RemoteException {
		Registry registry = LocateRegistry.getRegistry();
		Server obj;
		Remote stub;

		// with l digits and c query conditions we need at least 2*c*l+2 servers
		// (for sum operations; only 2*c*l+1 for count operations)
		int numServers = Configuration.numServers;

		for (int i = 0; i < numServers; i++) {
			obj = new Server();
			stub = UnicastRemoteObject.exportObject(obj, 0);
			registry.rebind("server-" + i, stub);
		}

		System.out.println(numServers + " servers started!");

		Starter.notifyStarter();
	}

	protected List<List<TranslatedQueryCondition>> rewriteTheQuery(Query query, List<String> credentials){
		if (batchedFileTable.isPolicyAttached()) {
			List<List<TranslatedQueryCondition>> policyConditions = new ArrayList<>();
			for (String credential : credentials) {
				Map<String, List<Integer>> policyConditionsOfCredential = new HashMap<>();
				if (batchedFileTable.isColumnPolicy()) {
					for (TranslatedQueryCondition queryCondition : query.getConditions()) {
						policyConditionsOfCredential.put(queryCondition.getAttributeName() + "p", policyOfCredsTable.get(credential));
					}
					if (query.getAggregateType() == AggregateType.COUNT && Objects.equals(query.getAttribute(), "*")) {
						// count(*) for attribute-level policy: only accept if the user has access to entire tuple
						for (String attributeName : batchedFileTable.getAttributeNames()) {
							if (attributeName.endsWith("p") && !batchedFileTable.getAttributeNames().contains(attributeName + "p"))
								policyConditionsOfCredential.put(attributeName, policyOfCredsTable.get(credential));
						}
					} else {
						policyConditionsOfCredential.put(query.getAttribute()  + "p", policyOfCredsTable.get(credential));
					}
				} else {
					policyConditionsOfCredential.put("tuplep", policyOfCredsTable.get(credential));
				}
				List<TranslatedQueryCondition> groupConditions = new ArrayList<>();
				for (Map.Entry<String, List<Integer>> groupCondition : policyConditionsOfCredential.entrySet()) {
					groupConditions.add(new TranslatedQueryCondition(groupCondition.getKey(), groupCondition.getValue()));
				}
				policyConditions.add(groupConditions);
			}
			return policyConditions;
		}
		return new ArrayList<>();
	}

	@Override
	public BigInteger sendCountQueryAndCredentials(Query query, List<String> credentials) throws RemoteException, QueryProcessingException {
		List<List<TranslatedQueryCondition>> policyConditions = rewriteTheQuery(query, credentials);

		BigInteger result = new BigInteger("0");

		Map<String, List<Integer>> nextTupleBatch;
		boolean resetReader = true;
		while ((nextTupleBatch = batchedFileTable.nextTupleBatch(batchSize*sharesPerValue, resetReader)) != null) {
			List<BigInteger> resultOfSMP = getStringMatchingResults(query, policyConditions, nextTupleBatch);
			for (BigInteger bigInteger : resultOfSMP) {
				result = result.add(bigInteger);
			}
			if (resetReader) resetReader = false;
		}

		return result;
	}

	@Override
	public BigInteger[][] sendSumQueryAndCredentials(Query query, List<String> credentials) throws RemoteException, QueryProcessingException {
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
		while ((nextTupleBatch = batchedFileTable.nextTupleBatch(batchSize*sharesPerValue, resetReader)) != null) {
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

			if (resetReader) resetReader = false;
		}

		return result;
	}

	@Override
	public List<Object> sendAvgQueriesAndCredentials(Query sumQuery, Query countQuery, List<String> credentials) throws RemoteException, QueryProcessingException {
		List<Object> result = new ArrayList<>();
		BigInteger[][] sumResult = sendSumQueryAndCredentials(sumQuery, credentials);
		BigInteger countResult = sendCountQueryAndCredentials(countQuery, credentials);
		result.add(0, countResult);
		result.add(1, sumResult);
		return result;
	}
	//This method gets the table from the data admin and also gets how many ciphers has the largest number in this table
	@Override
	public void sendTable(String tableName, Map<String, List<Integer>> table, boolean initialize, int sharesPerValue, int serverNumber, boolean columnPolicy) throws RemoteException {
		if (initialize) {
			this.sharesPerValue = sharesPerValue;
			this.batchedFileTable = new BatchedFileTable(table.keySet().stream().toList(), "src/main/resources/" + tableName + "-" + serverNumber + ".tbl", Configuration.attachPolicy, columnPolicy);
		}

		try {
			batchedFileTable.appendAttributeTable(table, initialize);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw new RuntimeException(e);
		}
	}

	@Override
	public void sendUnaryTranslationMeta(Map<String, Integer> unaryTranslationMeta) {
		this.unaryTranslationMeta = unaryTranslationMeta;
	}

	//This method is maybe relevant for Anna to add policies for the credential
	@Override
	public void addSharesForCredential(String credential, List<Integer> secretShares) {
		policyOfCredsTable.put(credential, secretShares);
	}
	private List<BigInteger> stringMatchingOperator(String table, List<Integer> conditions, List<Integer> tableOfAllSecrets) throws QueryProcessingException {
		List<BigInteger> result = new ArrayList<BigInteger>();
		if (!unaryTranslationMeta.containsKey(table)) throw new QueryProcessingException("Unknown table: " + table);
		int nrOfSecretsPerSymbol = sharesPerValue / unaryTranslationMeta.get(table);

		//here we go through all secrets of all attributes in the attribute table
		for(int i = 0; i < tableOfAllSecrets.size(); i = i + sharesPerValue) {
			BigInteger prodOnDifferentFigures = new BigInteger("1");
			//here we go through all the conditions that are in the where clausel
			for(int j = 0; j < sharesPerValue; j = j + nrOfSecretsPerSymbol) {
				BigInteger sum = new BigInteger("0");
				//here we do the operation for single ciphers
				for (int k = 0; k < nrOfSecretsPerSymbol; k++) {
					int prodOnTheSameFigure = tableOfAllSecrets.get(i + k + j);
					if (conditions != null) prodOnTheSameFigure *= conditions.get(k + j);
					sum = sum.add(new BigInteger(String.valueOf(prodOnTheSameFigure)));
				}
				prodOnDifferentFigures = prodOnDifferentFigures.multiply(sum);
			}
			result.add(prodOnDifferentFigures);
		}
		return result;
	}
	//here we do the string matching operation for all the attributes in the where clausel
	private List<List<BigInteger>> stringMatchingOperatorForAllConditions(String table, List<TranslatedQueryCondition> conditions, Map<String, List<Integer>> allSecretsOfAttributes) throws QueryProcessingException {
		List<List<BigInteger>> result = new ArrayList<>();
		for(TranslatedQueryCondition queryCondition : conditions) {
			if(allSecretsOfAttributes.containsKey(queryCondition.getAttributeName())){
				result.add(stringMatchingOperator(table, queryCondition.getValueShares(), allSecretsOfAttributes.get(queryCondition.getAttributeName())));
			}
		}
		return result;
	}
	//here we multiply all the results that came from the method before
	protected List<BigInteger> getStringMatchingResults(Query query, List<List<TranslatedQueryCondition>> policyConditions, Map<String, List<Integer>> allSecretsOfAttributes) throws QueryProcessingException {
		List<BigInteger> result = new ArrayList<>();
		List<List<BigInteger>> queryConditionResults = stringMatchingOperatorForAllConditions(query.getTable(), query.getConditions(), allSecretsOfAttributes);
		List<List<BigInteger>> policyConditionResults = stringMatchingOperatorForPolicyConditions(query.getTable(), policyConditions, allSecretsOfAttributes);

		// pseudo string-matching over one attribute if no policy is attached and the query has no conditions
		if (queryConditionResults.isEmpty() && policyConditionResults.isEmpty()) {
			if (query.getAggregateType() == AggregateType.COUNT && Objects.equals(query.getAttribute(), "*")) {
				return stringMatchingOperator(query.getTable(), null, allSecretsOfAttributes.get(batchedFileTable.getAttributeNames().get(0)));
			} else {
				return stringMatchingOperator(query.getTable(), null, allSecretsOfAttributes.get(query.getAttribute()));
			}
		}

		// return result directly if there is only one
		else if (queryConditionResults.size() == 1 && policyConditionResults.isEmpty()) {
			return queryConditionResults.get(0);
		} else if (queryConditionResults.isEmpty() && policyConditionResults.size() == 1) {
			return policyConditionResults.get(0);
		}

		// handle exclusively conjunctive or disjunctive query conditions
		for (List<BigInteger> attributeResult : queryConditionResults) {
			for (int i = 0; i < attributeResult.size(); i++) {
				BigInteger singleResult = attributeResult.get(i);
				if (result.size() <= i) result.add(attributeResult.get(i));
				else if (query.getConditionalType() == ConditionalType.CONJUNCTIVE || query.getConditionalType() == null) {
					result.set(i, result.get(i).multiply(singleResult));
				} else if (query.getConditionalType() == ConditionalType.DISJUNCTIVE) {
					result.set(i, result.get(i).add(singleResult).subtract(result.get(i).multiply(singleResult)));
				}
			}
		}

		// multiply by results of policy string-matching (-> ANDs over policy conditions)
		for(List<BigInteger> attributePolicyResult : policyConditionResults) {
			for (int i = 0; i < attributePolicyResult.size(); i++) {
				if (result.size() <= i) result.add(attributePolicyResult.get(i));
				else result.set(i, result.get(i).multiply(attributePolicyResult.get(i)));
			}
		}

		return result;
	}

	private List<List<BigInteger>> stringMatchingOperatorForPolicyConditions(String table, List<List<TranslatedQueryCondition>> policyConditions, Map<String, List<Integer>> allSecretsOfAttributes) throws QueryProcessingException {
		List<List<List<BigInteger>>> stringMatchingResultsOfGroups = new ArrayList<>();
		List<List<BigInteger>> stringMatchingResultsOfAttributes = new ArrayList<>();
		int numPolicyAttributes = 0;
		for (List<TranslatedQueryCondition> groupConditions : policyConditions) {
			if (numPolicyAttributes == 0) numPolicyAttributes = groupConditions.size();
			stringMatchingResultsOfGroups.add(stringMatchingOperatorForAllConditions(table, groupConditions, allSecretsOfAttributes));
		}
		for (int i = 0; i < numPolicyAttributes; i++) {
			stringMatchingResultsOfAttributes.add(new ArrayList<>());
			for (List<List<BigInteger>> groupResult : stringMatchingResultsOfGroups) {
				// OR condition over groups that the user belongs to (for each policy attribute)
				for (int j = 0; j < groupResult.get(i).size(); j++) {
					List<BigInteger> currentAttributeResult = stringMatchingResultsOfAttributes.get(i);
					BigInteger attributeSingeResult = groupResult.get(i).get(j);

					if (currentAttributeResult.size() <= j) currentAttributeResult.add(attributeSingeResult);
					else currentAttributeResult.set(j, currentAttributeResult.get(j).add(attributeSingeResult).subtract(currentAttributeResult.get(j).multiply(attributeSingeResult)));
				}
			}
		}
		return stringMatchingResultsOfAttributes;
	}
}
