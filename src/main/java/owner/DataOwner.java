package owner;

import common.IDataOwner;
import common.IServer;
import common.SecretCreator;
import common.UnaryTranslator;
import manual.Configuration;
import owner.authorizations.CredentialManager;
import owner.authorizations.PolicyManager;
import owner.lineitem.LineitemUploader;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

public class DataOwner implements IDataOwner {

    private static final int POLY_DEGREE = 1;

    private static CredentialManager credentialManager;

    private static PolicyManager policyManager;

    private DataOwner() {
        UnaryTranslator.initUnaryTranslatorSingleton(new HashMap<>());
        SecretCreator.initSecretCreatorSingleton(POLY_DEGREE);

        credentialManager = new CredentialManager();
        policyManager = new PolicyManager();
    }

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry();

        DataOwner obj = new DataOwner(); // initializes UnaryTranslator and SecretCreator singletons
        Remote stub = UnicastRemoteObject.exportObject(obj, 0);
        registry.rebind("data-owner", stub);

        List<IServer> serverStubs = new ArrayList<>();

        // assume all servers are available and match the expression "serverX" where X \in {1,...,|serverStubs|} is the
        // value used to create the secret shares stored in the database for serverX
        for (String name : Arrays.stream(registry.list()).sorted().collect(Collectors.toList())) {
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

        Scanner scanner = new Scanner(System.in);
        while (true) {

            boolean correct_be=false;
            HashMap<Integer, List<Integer>> be_group = new HashMap<>();
            while(!correct_be){
                System.out.println("Enter a boolean expression to express your policy logic:");
                System.out.print(">> ");
                String policyString = scanner.nextLine();
                String normalization = policyString.replace("and","").replace("or","").replace("(","").replace(")","");

                try{
                    normalization = normalization.replaceAll("\\d", "").strip();
                    if(normalization.isEmpty()){
                        be_group = policyManager.createBooleanExpression(policyString);
                        correct_be = true;
                    }else{
                        System.out.print("Incorrect characters");
                    }
                }catch(Exception e){
                    System.out.println(e);
                    System.out.println("Please try again");
                }
            }

            System.out.println("Enter 'y' if your policy is tuple-based:");
            System.out.print(">> ");
            boolean tuple_based = true;
            if(!scanner.nextLine().equals("y")) {
                tuple_based=false;
            }

            System.out.println("If you selected an tuple-based policy, please enter the primary key otherwise enter the column-name for which the policy is applicable:");
            System.out.print(">> ");
            String condition = scanner.nextLine();

            System.out.println("Enter 'y' if your policy is for 'allow' permissions:");
            System.out.print(">> ");
            boolean permission = true;
            if(!scanner.nextLine().equals("y")) {
                permission=false;
            }

            //default: 1 for primary key
            int condition_type=1;
            policyManager.createACPs(be_group, tuple_based, condition, condition_type, permission);
            /*if (allowed_chars.matches("^[0-9 ()<>=]+$")) {
                policyManager.createBooleanExpression("(1 OR 2) AND 3");
            } else {
                System.out.println("Incorrect Boolean Expression given");
            }*/
        }
    }

    @Override
    public Map<String, Integer> getUnaryTranslationMetaData() {
        return UnaryTranslator.getUnaryTranslatorSingleton().getUnaryTranslationMeta();
    }


    @Override
    public String createCredentials(HashMap<String, List<String>> user_attr) throws RemoteException {
        String token = credentialManager.getUserCredentials(user_attr);
        return token;
    }

}
