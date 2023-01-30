package manual;

import owner.DataOwner;
import server.Server;
import user.DataUser;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Starter {

  public static void main(String[] args) throws RemoteException, InterruptedException {
    Runnable registry = () -> {
      try {
        RegistryHost.main(null);
      } catch (RemoteException | InterruptedException e) {
        throw new RuntimeException(e);
      }
    };

    Thread registryThread = new Thread(registry);
    registryThread.start();

    synchronized (registryThread) {
      registryThread.wait();
    }


    Runnable servers = () -> {
      try {
        Server.main(null);
      } catch (RemoteException e) {
        throw new RuntimeException(e);
      }
    };

    Thread serversThread = new Thread(servers);
    serversThread.start();

    synchronized (serversThread) {
      serversThread.wait();
    }


    Runnable dataOwner = () -> {
      try {
        DataOwner.main(null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };

    Thread dataOwnerThread = new Thread(dataOwner);
    dataOwnerThread.start();

    synchronized (dataOwnerThread) {
      dataOwnerThread.wait();
    }


    Runnable dataUser = () -> {
      try {
        DataUser.main(null);
      } catch (NotBoundException | RemoteException e) {
        throw new RuntimeException(e);
      }
    };

    Thread dataUserThread = new Thread(dataUser);
    dataUserThread.start();
  }

  public static void notifyStarter() {
    synchronized (Thread.currentThread()) {
      Thread.currentThread().notify();
    }
  }

}
