import user.exceptions.QueryParsingException;
import user.exceptions.QueryProcessingException;
import user.queryparsing.ParsedQuery;
import user.queryparsing.QueryParser;
import common.Query;
import user.queryprocessing.QuerySplitter;
import user.queryprocessing.QueryTranslator;
import common.AggregateType;
import common.ConditionalType;
import common.SecretCreator;
import common.UnaryTranslator;
import owner.lineitem.LineitemAttributes;

import java.util.ArrayList;
import java.util.List;

public class QueryCreator {

    private static final int[] queryConditionValues = new int[]{1319,36685,1692,2};

    public static List<Query> getAllQueries() throws Exception {
        List<Query> queries = new ArrayList<>();
        for (ConditionalType c : ConditionalType.values()) {
            for (AggregateType t : AggregateType.values()) {
                for (int numConditions : new int[]{0,2,4}) {
                    queries.add(getQuery(t, c, numConditions));
                }
            }
        }
        return queries;
    }

    public static Query getQuery(AggregateType t, ConditionalType c, int numConditions) throws Exception {
        switch (t) {
            case COUNT -> { return getCountQuery(numConditions, c); }
            case SUM -> { return getSumQuery(numConditions, c); }
            case AVG -> { return getAvgQuery(numConditions, c); }
            default -> throw new Exception();
        }
    }

    public static Query getCountQuery(int numConditions, ConditionalType c) throws QueryParsingException, QueryProcessingException {
        String queryString = buildQuery(AggregateType.COUNT,"orderkey", c, numConditions);
        System.out.println(queryString);
        return getQueryFromString(queryString);
    }

    public static Query getSumQuery(int numConditions, ConditionalType c) throws QueryParsingException, QueryProcessingException {
        String queryString = buildQuery(AggregateType.SUM, "orderkey", c, numConditions);
        System.out.println(queryString);
        return getQueryFromString(queryString);
    }

    public static Query getAvgQuery(int numConditions, ConditionalType c) throws QueryParsingException, QueryProcessingException {
        String queryString = buildQuery(AggregateType.AVG, "orderkey", c, numConditions);
        System.out.println(queryString);
        return getQueryFromString(queryString);
    }

    private static String buildQuery(AggregateType t, String attribute, ConditionalType c, int numConditions) {
        StringBuilder query = new StringBuilder("select " + t.token + "(" + attribute + ") from lineitem");

        if (numConditions == 0) return query + ";";

        for (int i = 0; i < numConditions; i++) {
            if (i == 0) query.append(" where ");
            else if (c == ConditionalType.CONJUNCTIVE) query.append(" and ");
            else query.append(" or ");
            query.append(LineitemAttributes.attributeNames[i]).append("=").append(queryConditionValues[i]);
        }

        return query.append(";").toString();
    }

    private static Query getQueryFromString(String queryString) throws QueryParsingException, QueryProcessingException {
        ParsedQuery parsedQuery = QueryParser.parseQuery(queryString);
        Query translatedQuery = QueryTranslator.translateQueryConditionValues(parsedQuery, UnaryTranslator.getUnaryTranslatorSingleton());
        List<Query> queries = QuerySplitter.splitQuery(translatedQuery, 1, SecretCreator.getSecretCreatorSingleton());
        return queries.get(0);
    }

}
