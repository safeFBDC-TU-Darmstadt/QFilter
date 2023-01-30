package owner;

import common.IDataOwner;
import common.IServer;
import common.SecretCreator;
import common.UnaryTranslator;
import manual.Configuration;
import manual.Starter;
import owner.lineitem.LineitemUploader;

import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class DataOwner implements IDataOwner {

    private static final int POLY_DEGREE = 1;

    private DataOwner() {
        UnaryTranslator.initUnaryTranslatorSingleton(new HashMap<>());
        SecretCreator.initSecretCreatorSingleton(POLY_DEGREE);
    }

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry();

        DataOwner obj = new DataOwner(); // initializes UnaryTranslator and SecretCreator singletons
        Remote stub = UnicastRemoteObject.exportObject(obj, 0);
        registry.rebind("data-owner", stub);

        List<IServer> serverStubs = new ArrayList<>();

        // assume all servers are available and match the expression "serverX" where X \in {1,...,|serverStubs|} is the
        // value used to create the secret shares stored in the database for serverX
        for (String name : Arrays.stream(registry.list()).sorted().toList()) {
            if (name.startsWith("server")) serverStubs.add((IServer) registry.lookup(name));
        }

        int NUM_GROUPS = Configuration.NUM_GROUPS;

        LineitemUploader.uploadTables(Configuration.batchSize, Configuration.rowLimit, Configuration.columnPolicy, NUM_GROUPS, Configuration.accessibilityPercentage, serverStubs);

        for (IServer server : serverStubs)
            server.sendUnaryTranslationMeta(UnaryTranslator.getUnaryTranslatorSingleton().getUnaryTranslationMeta());

        // send secret shares of group numbers to all servers
        for (int i = 1; i <= NUM_GROUPS; i++) {
            List<Integer> translatedGroupNumber = UnaryTranslator.getUnaryTranslatorSingleton().translatePositiveIntegerString("lineitem", String.valueOf(i));
            List<List<Integer>> groupNumberSecretShares = SecretCreator.getSecretCreatorSingleton().createSecrets(translatedGroupNumber, serverStubs.size());
            for (int j = 0; j < serverStubs.size(); j++) {
                serverStubs.get(j).addSharesForCredential(String.valueOf(i), groupNumberSecretShares.get(j));
            }
        }

        System.out.println("finished uploading tables!");

        Starter.notifyStarter();
    }

    @Override
    public Map<String, Integer> getUnaryTranslationMetaData() {
        return UnaryTranslator.getUnaryTranslatorSingleton().getUnaryTranslationMeta();
    }

}
