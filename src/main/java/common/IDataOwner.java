package common;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IDataOwner extends java.rmi.Remote {
    Map<String, Integer> getUnaryTranslationMetaData() throws RemoteException;

    String createCredentials(HashMap<String, List<String>> user_attr) throws RemoteException;
    //String createCredentials(String user_attr) throws RemoteException;

}
