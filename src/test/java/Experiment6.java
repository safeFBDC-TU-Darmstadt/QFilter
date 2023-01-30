import user.exceptions.QueryParsingException;
import user.exceptions.QueryProcessingException;
import user.queryparsing.ParsedQuery;
import user.queryparsing.QueryParser;
import common.Query;
import user.queryprocessing.QuerySplitter;
import user.queryprocessing.QueryTranslator;
import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import owner.lineitem.LineitemAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Measures the time to split a query with a variable number of conditions (always combined by AND).
public class Experiment6 {

    int[] queryConditions = new int[]{0,2,4,0,2,4}; // experiment is run twice: first time to set up; only the
                                                    // second result is taken for stable outcomes (exclude caching, ...)

    @BeforeEach
    void init() {
        Map<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put("lineitem", LineitemAttributes.maxStringLength);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMeta);
        SecretCreator.initSecretCreatorSingleton(1);
    }

    @Test
    void measureTimes() throws QueryParsingException, QueryProcessingException {
        for (int numConditions : queryConditions) {
            String query = buildQuery(numConditions);
            ParsedQuery parsedQuery = QueryParser.parseQuery(query);
            Query translatedQuery = QueryTranslator.translateQueryConditionValues(parsedQuery, UnaryTranslator.getUnaryTranslatorSingleton());
            long start = System.nanoTime();
            List<Query> queries = QuerySplitter.splitQuery(translatedQuery, 15, SecretCreator.getSecretCreatorSingleton());
            long time = System.nanoTime() - start;
            System.out.println("time for query splitting (query=\"" + query + "\"): " + time);
        }
    }

    String buildQuery(int numConditions) {
        StringBuilder query = new StringBuilder("select count(*) from lineitem");

        if (numConditions == 0) return query + ";";

        for (int i = 0; i < numConditions; i++) {
            if (i == 0) query.append(" where ").append(LineitemAttributes.attributeNames[i]).append("=").append(i);
            else query.append(" and ").append(LineitemAttributes.attributeNames[i]).append("=").append(i);
        }

        return query.append(";").toString();
    }

}
