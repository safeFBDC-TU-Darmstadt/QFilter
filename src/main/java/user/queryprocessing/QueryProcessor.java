package user.queryprocessing;

import user.exceptions.QueryProcessingException;
import user.queryparsing.ParsedQuery;
import common.*;
import owner.lineitem.LineitemAttributes;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class QueryProcessor {
    private final List<IServer> serverStubs;
    private final List<String> credentials;
    private final UnaryTranslator unaryTranslator;
    private final SecretCreator secretCreator;

    public QueryProcessor(List<IServer> serverStubs, List<String> credentials, Map<String, Integer> unaryTranslationMeta, int polyDegree) {
        this.serverStubs = serverStubs;
        this.credentials = credentials;
        this.unaryTranslator = UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMeta);
        this.secretCreator = SecretCreator.initSecretCreatorSingleton(polyDegree);
    }

    public String handleQuery(ParsedQuery query) throws QueryProcessingException {
        switch (query.getAggregateType()) {
            case COUNT, SUM -> { return handleCountOrSumQuery(query); }
            case AVG -> { return handleAvgQuery(query); }
            default -> throw new QueryProcessingException("Unsupported aggregation " + query.getAggregateType());
        }
    }

    private String handleCountOrSumQuery(ParsedQuery query) throws QueryProcessingException {
        Query translatedQuery = QueryTranslator.translateQueryConditionValues(query, unaryTranslator);
        List<Query> queries = QuerySplitter.splitQuery(translatedQuery, serverStubs.size(), secretCreator);

        switch (query.getAggregateType()) {
            case COUNT -> {
                List<BigInteger> serverResults = QueryDistributor.distributeCountQueries(queries, serverStubs, credentials);
                return String.valueOf(ResultCollector.getCountResult(serverResults));
            }
            case SUM -> {
                List<BigInteger[][]> serverResults = QueryDistributor.distributeSumQueries(queries, serverStubs, credentials);
                if (!query.getTable().equalsIgnoreCase("lineitem")) throw new QueryProcessingException("Table " + query.getTable() + " unsupported.");
                return ResultCollector.getSumResultBase10(serverResults, LineitemAttributes.attributeTypes.get(query.getAttribute()), LineitemAttributes.decimalPlaces);
            }
            default -> throw new QueryProcessingException("Unsupported aggregation " + query.getAggregateType());
        }
    }

    private String handleAvgQuery(ParsedQuery query) throws QueryProcessingException {
        ParsedQuery sumQuery = new ParsedQuery(query);
        ParsedQuery countQuery = new ParsedQuery(query);

        sumQuery.setAggregateType(AggregateType.SUM);
        countQuery.setAggregateType(AggregateType.COUNT);

        Query translatedSumQuery = QueryTranslator.translateQueryConditionValues(sumQuery, unaryTranslator);
        List<Query> sumQueries = QuerySplitter.splitQuery(translatedSumQuery, serverStubs.size(), secretCreator);

        Query translatedCountQuery = QueryTranslator.translateQueryConditionValues(sumQuery, unaryTranslator);
        List<Query> countQueries = QuerySplitter.splitQuery(translatedCountQuery, serverStubs.size(), secretCreator);

        List<List<Query>> zippedQueries = new ArrayList<>();
        for (int i = 0; i < serverStubs.size(); i++) {
            List<Query> serverQueries = new ArrayList<>();
            serverQueries.add(sumQueries.get(i));
            serverQueries.add(countQueries.get(i));
            zippedQueries.add(serverQueries);
        }

        List<List<Object>> results = QueryDistributor.distributeAvgQueries(zippedQueries, serverStubs, credentials);

        List<BigInteger[][]> sumResults = new ArrayList<>();
        List<BigInteger> countResults = new ArrayList<>();

        for (List<Object> result : results) {
            sumResults.add((BigInteger[][]) result.get(1));
            countResults.add((BigInteger) result.get(0));
        }

        BigDecimal sumResult = new BigDecimal(ResultCollector.getSumResultBase10(sumResults, LineitemAttributes.attributeTypes.get(query.getAttribute()), LineitemAttributes.decimalPlaces));
        BigDecimal countResult = new BigDecimal(ResultCollector.getCountResult(countResults));

        return countResult.equals(new BigDecimal("0")) ? "0" : String.valueOf(sumResult.divide(countResult, 7, RoundingMode.DOWN));
    }

}
