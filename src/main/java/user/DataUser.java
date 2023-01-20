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
import owner.authorizations.CredentialManager;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.stream.Collectors;

public class DataUser {

    private static final int POLY_DEGREE = 1;

    private static String credentials="0";

    private DataUser() {
    }

    public static void main(String[] args) throws NotBoundException, RemoteException {
        HashMap<String, List<String>> attributes = new HashMap<>();
        // Intended as standard input for primary setup => so that there is a DataUser, tester can use.
        attributes.put("a",Arrays.asList("=,b".split(",")));
        attributes.put("isInitialized", Arrays.asList("=,true".split(",")));
        String data_owner="data-owner";
        String credentials="0";

        try (DB db = DBMaker.fileDB("src/main/resources/client.db").make();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            Object isAttributeMapInitialized = attributeMap.get("isInitialized");
            if (isAttributeMapInitialized == null)
                initializeAttributeMap(attributeMap);
        }

        Registry registry = LocateRegistry.getRegistry();

        // contains padding size of all attributes of all tables
        // example: unaryTranslationMeta.get("table");
        // (could extend data type 'varchar' later and store the datatype of each attribute, for now assume
        // all attributes are of type integer)
        Map<String, Integer> unaryTranslationMeta = ((IDataOwner) registry.lookup("data-owner")).getUnaryTranslationMetaData();

        CredentialManager credentialExchanger = new CredentialManager();

        List<IServer> serverStubs = new ArrayList<>();
        List<IDataOwner> ownerStub = new ArrayList<>();
        // assume all servers are available and match the expression "serverX" where X \in {1,...,|serverStubs|} is the
        // value used to create the secret shares stored in the database for serverX
        for (String name : Arrays.stream(registry.list()).sorted().collect(Collectors.toList())) {
            if (name.startsWith("server")) serverStubs.add((IServer) registry.lookup(name));
            if (name.equals(data_owner)) ownerStub.add((IDataOwner) registry.lookup(name));
        }

        if (credentials.equals("0")) {
            credentials = CredentialExchanger.getUserCredentials(ownerStub, attributes);
        }
        QueryProcessor queryProcessor =
                new QueryProcessor(serverStubs, Arrays.asList(credentials), unaryTranslationMeta, POLY_DEGREE);

        // read in & process queries; display results
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("Enter query or 'exit' to exit or 'attribute' to add new attribute:");
            System.out.print(">> ");
            String queryString = scanner.nextLine();

            if (queryString.trim().equalsIgnoreCase("exit")) break;
            else if (queryString.trim().equalsIgnoreCase("attribute")) {
                System.out.println("Enter key of attribute:");
                System.out.print(">> ");
                String attr_key = scanner.nextLine();
                System.out.println("Enter comparison method of attribute (=/</>/<=/<=):");
                System.out.print(">> ");
                List<String> allowed_comparators =  Arrays.asList("=","<",">","<=","<=");
                String attr_method = scanner.nextLine();
                while (!allowed_comparators.contains(attr_method)){
                    System.out.println("Please try again: Enter comparison method of attribute (=/</>/<=/<=):");
                    System.out.print(">> ");
                    attr_method = scanner.nextLine();
                }
                System.out.println("Enter value of attribute:");
                System.out.print(">> ");
                String attr_value = scanner.nextLine();
                attributes.put(attr_key, Arrays.asList(attr_method,attr_value));
                credentials = CredentialExchanger.getUserCredentials(ownerStub, attributes);
            }
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
        attributeMap.put("isInitialized", "=,true");
    }

}
