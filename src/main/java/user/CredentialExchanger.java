package user;

import common.IDataOwner;

import java.util.HashMap;
import java.util.List;

public class CredentialExchanger {

    public static String getUserCredentials(List<IDataOwner> ownerStub, HashMap<String, List<String>> attributeMap) {
        try {
            return ownerStub.get(0).createCredentials(attributeMap);
        }catch (Exception e) {
            System.out.println("Exception during Credential Exchange");
            System.out.println(e);
            return "No Credentials";
        }
    }

}
