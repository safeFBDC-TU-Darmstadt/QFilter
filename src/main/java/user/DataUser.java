package user;

import user.exceptions.QueryParsingException;
import user.exceptions.QueryProcessingException;
import user.queryparsing.ParsedQuery;
import user.queryparsing.QueryParser;
import user.queryprocessing.QueryProcessor;
import common.IDataOwner;
import common.IServer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class DataUser {

    private static final int POLY_DEGREE = 1;

    private DataUser() {
    }

    public static void main(String[] args) throws NotBoundException, RemoteException {
        String[] attributes;

        try (DB db = DBMaker.fileDB("src/main/resources/client.db").make();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            Object isAttributeMapInitialized = attributeMap.get("isInitialized");
            if (isAttributeMapInitialized == null)
                initializeAttributeMap(attributeMap);

            attributes = getAttributes(attributeMap);
        }

        Registry registry = LocateRegistry.getRegistry();

        // contains padding size of all attributes of all tables
        // example: unaryTranslationMeta.get("table");
        // (could extend data type 'varchar' later and store the datatype of each attribute, for now assume
        // all attributes are of type integer)
        Map<String, Integer> unaryTranslationMeta = ((IDataOwner) registry.lookup("data-owner")).getUnaryTranslationMetaData();

        // System.out.println(unaryTranslationMeta);

        CredentialExchanger credentialExchanger = new CredentialExchanger();

        List<IServer> serverStubs = new ArrayList<>();
        // assume all servers are available and match the expression "serverX" where X \in {1,...,|serverStubs|} is the
        // value used to create the secret shares stored in the database for serverX
        for (String name : Arrays.stream(registry.list()).sorted().toList()) {
            if (name.startsWith("server")) serverStubs.add((IServer) registry.lookup(name));
        }

        QueryProcessor queryProcessor =
                new QueryProcessor(serverStubs, credentialExchanger.getUserCredentials(attributes), unaryTranslationMeta, POLY_DEGREE);

        // read in & process queries; display results
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter query or 'exit' to exit:");
            System.out.print(">> ");
            String queryString = scanner.nextLine();

            if (queryString.trim().equalsIgnoreCase("exit")) break;
            else {
                try {
                    ParsedQuery parsedQuery = QueryParser.parseQuery(queryString);
                    String result = queryProcessor.handleQuery(parsedQuery);
                    System.out.println("\n  Result: " + result + "\n");
                } catch (QueryParsingException | QueryProcessingException e) {
                    System.out.println("\n" + e.getMessage() + "\n");
                }
            }
        }
    }

    private static void initializeAttributeMap(HTreeMap<String, String> attributeMap) {
        attributeMap.put("name", "abc");
        attributeMap.put("age", "42");
        attributeMap.put("group", "dm-lab");
        attributeMap.put("isInitialized", "true");
    }

    private static String[] getAttributes(HTreeMap<String, String> attributeMap) {
        return new String[]{attributeMap.get("name"), attributeMap.get("age"), attributeMap.get("group")};
    }
}
