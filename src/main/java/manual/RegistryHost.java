package manual;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class RegistryHost {

    public static void main(String[] args) throws RemoteException, InterruptedException {
        Registry r = LocateRegistry.createRegistry(1099);
        Starter.notifyStarter();
        Thread.currentThread().join(); // waits for a join forever without using a busy loop
    }

}
