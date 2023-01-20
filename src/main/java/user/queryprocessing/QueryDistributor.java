package user.queryprocessing;

import user.exceptions.QueryProcessingException;
import common.IServer;
import common.Query;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.List;
import java.util.stream.Collectors;

public abstract class QueryDistributor {

    public static List<BigInteger> distributeCountQueries(List<Query> queries, List<IServer> serverStubs, List<String> credentials) {
        // use a parallel stream to process the queries in parallel; the map function will also keep the order of the results
        return queries.parallelStream().map(q -> {
            try {
                return serverStubs.get(q.getServerIdx()).sendCountQueryAndCredentials(q, credentials);
            } catch (RemoteException | QueryProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    public static List<BigInteger[][]> distributeSumQueries(List<Query> queries, List<IServer> serverStubs, List<String> credentials) {
        // use a parallel stream to process the queries in parallel; the map function will also keep the order of the results
        return queries.parallelStream().map(q -> {
            try {
                return serverStubs.get(q.getServerIdx()).sendSumQueryAndCredentials(q, credentials);
            } catch (RemoteException | QueryProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    public static List<List<Object>> distributeAvgQueries(List<List<Query>> queries, List<IServer> serverStubs, List<String> credentials) {
        // use a parallel stream to process the queries in parallel; the map function will also keep the order of the results
        return queries.parallelStream().map(q -> {
            try {
                return serverStubs.get(q.get(0).getServerIdx()).sendAvgQueriesAndCredentials(q.get(0), q.get(1), credentials);
            } catch (RemoteException | QueryProcessingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

}
