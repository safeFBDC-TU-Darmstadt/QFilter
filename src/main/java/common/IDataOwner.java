package common;

import java.rmi.RemoteException;
import java.util.Map;

public interface IDataOwner extends java.rmi.Remote {

    Map<String, Integer> getUnaryTranslationMetaData() throws RemoteException;

}
