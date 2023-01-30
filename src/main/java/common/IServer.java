package common;

import user.exceptions.QueryProcessingException;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

public interface IServer extends java.rmi.Remote {
    BigInteger sendCountQueryAndCredentials(Query query, List<String> credentials) throws RemoteException, QueryProcessingException;
    BigInteger[][] sendSumQueryAndCredentials(Query query, List<String> credentials) throws RemoteException, QueryProcessingException;
    List<Object> sendAvgQueriesAndCredentials(Query sumQuery, Query countQuery, List<String> credentials) throws RemoteException, QueryProcessingException;
    void sendTable(String tableName, Map<String, List<Integer>> table, boolean initialize, int sharesPerValue, int serverNumber, boolean columnPolicy) throws RemoteException;
    void sendUnaryTranslationMeta(Map<String, Integer> unaryTranslationMeta) throws RemoteException;
    void addSharesForCredential(String credential, List<Integer> secretShares) throws RemoteException;
}
