package user.queryprocessing;

import common.Query;
import common.SecretCreator;
import common.TranslatedQueryCondition;

import java.util.ArrayList;
import java.util.List;

public abstract class QuerySplitter {

    public static List<Query> splitQuery(Query query, int numServers, SecretCreator secretCreator) {
        List<Query> splitQueries = new ArrayList<>();
        for (int i = 0; i < numServers; i++)
            splitQueries.add(new Query(query.getAggregateType(), query.getAttribute(),
                    query.getTable(), query.getConditionalType(), new ArrayList<>(), i));

        for (TranslatedQueryCondition queryCondition : query.getConditions()) {
            List<List<Integer>> secrets = secretCreator.createSecrets(queryCondition.getValueShares(), numServers);
            for (int i = 0; i < numServers; i++) {
                splitQueries.get(i).getConditions().add(new TranslatedQueryCondition(queryCondition.getAttributeName(), secrets.get(i)));
            }
        }
        return splitQueries;
    }

}
