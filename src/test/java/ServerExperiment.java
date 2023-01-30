import user.exceptions.QueryProcessingException;
import common.Query;
import common.AggregateType;
import common.SecretCreator;
import common.UnaryTranslator;
import org.junit.jupiter.api.BeforeEach;
import owner.lineitem.LineitemAttributes;
import server.measurements.MeasurementServer;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerExperiment {

    public ServerExperiment() {
        init();
    }

    @BeforeEach
    void init() {
        Map<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put("lineitem", LineitemAttributes.maxStringLength);
        UnaryTranslator.initUnaryTranslatorSingleton(unaryTranslationMeta);
        SecretCreator.initSecretCreatorSingleton(1);
    }

    long getProcessingTime(MeasurementServer server, Query query) throws RemoteException, QueryProcessingException {
        long time = -1;
        switch (query.getAggregateType()) {
            case COUNT ->
                    time = server.sendCountQueryAndCredentials(query, Collections.singletonList("1"), true);
            case SUM ->
                    time = server.sendSumQueryAndCredentials(query, Collections.singletonList("1"), true);
            case AVG -> {
                Query countQuery = new Query(AggregateType.COUNT, query.getAttribute(), query.getTable(), query.getConditionalType(), new ArrayList<>(query.getConditions()));
                Query sumQuery = new Query(AggregateType.SUM, query.getAttribute(), query.getTable(), query.getConditionalType(), new ArrayList<>(query.getConditions()));
                time = server.sendCountQueryAndCredentials(countQuery, Collections.singletonList("1"), true);
                time += server.sendSumQueryAndCredentials(sumQuery, Collections.singletonList("1"), true);
            }
        }
        return time;
    }

    void setUnaryTranslationMeta(MeasurementServer server) {
        HashMap<String, Integer> unaryTranslationMeta = new HashMap<>();
        unaryTranslationMeta.put(LineitemAttributes.tableName, LineitemAttributes.maxStringLength);
        server.sendUnaryTranslationMeta(unaryTranslationMeta);
    }

}
