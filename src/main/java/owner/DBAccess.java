package owner;

import common.Attribute;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import owner.authorizations.BooleanExpression;

import java.util.*;

public abstract class DBAccess {
    public static HashMap<Integer, BooleanExpression> readBoolExpMap(){
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> boolExpMap =
                     (HTreeMap<String, String>) db.hashMap("boolExpMap").createOrOpen()) {
            HashMap<Integer, BooleanExpression> booleanExpressions = new HashMap<>();
            for (Object beM : boolExpMap.keySet()) {
                if (!"isInitialized".equals(beM.toString())) {
                    List<String> strList = Arrays.asList(boolExpMap.get(beM).split(","));
                    BooleanExpression be = new BooleanExpression(Integer.valueOf(strList.get(0)), Integer.valueOf(strList.get(1)), strList.get(2), strList.get(5));
                    List<String> attrList1 = Arrays.asList(strList.get(3).split("|"));
                    List<String> attrList2 = Arrays.asList(strList.get(4).split("|"));
                    List<Integer> intList1 = new ArrayList<>();
                    List<Integer> intList2 = new ArrayList<>();
                    for (String s : attrList1) intList1.add(Integer.valueOf(s));
                    for (String s : attrList2) intList2.add(Integer.valueOf(s));
                    be.setApplicable_attr1(intList1);
                    be.setApplicable_attr1(intList2);
                    int combinExpr = Integer.valueOf(strList.get(6));
                    be.setCombinationExpression(combinExpr);
                    booleanExpressions.put(Integer.parseInt(beM.toString()), be);
                }
            }
            return booleanExpressions;
        }
    }

    public static HashMap<Integer, BooleanExpression> readOpenBoolExpMap(HTreeMap<String, String> boolExpMap){
        HashMap<Integer, BooleanExpression> booleanExpressions = new HashMap<>();
        for (Object beM : boolExpMap.keySet()) {
            if (!"isInitialized".equals(beM.toString())) {
                List<String> strList = Arrays.asList(boolExpMap.get(beM).split(","));
                BooleanExpression be = new BooleanExpression(Integer.valueOf(strList.get(0)), Integer.valueOf(strList.get(1)), strList.get(2), strList.get(5));
                List<String> attrList1 = Arrays.asList(strList.get(3).split("|"));
                List<String> attrList2 = Arrays.asList(strList.get(4).split("|"));
                List<Integer> intList1 = new ArrayList<>();
                List<Integer> intList2 = new ArrayList<>();
                for (String s : attrList1) intList1.add(Integer.valueOf(s));
                for (String s : attrList2) intList2.add(Integer.valueOf(s));
                be.setApplicable_attr1(intList1);
                be.setApplicable_attr2(intList2);
                int combinExpr = Integer.valueOf(strList.get(6));
                be.setCombinationExpression(combinExpr);
                booleanExpressions.put(Integer.parseInt(beM.toString()), be);
            }
        }
        return booleanExpressions;
    }

    public static HashMap<Integer, List<Integer>> readUserGroupMap(){
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> userGroupMap =
                     (HTreeMap<String, String>) db.hashMap("userGroupMap").createOrOpen()) {
            HashMap<Integer, List<Integer>> userGroup_be = new HashMap<>();
            for (Object uG : userGroupMap.keySet()) {
                if (!"isInitialized".equals(uG.toString())) {
                    List<Integer> intList = new ArrayList<>();
                    String str = userGroupMap.get(uG).replaceAll("[\\[\\]]", "");
                    str = str.replaceAll(" ", "");
                    List<String> strList = Arrays.asList(str.split(","));
                    for (String s : strList) intList.add(Integer.valueOf(s));
                    userGroup_be.put(Integer.valueOf(uG.toString()), intList);
                }
            }
            return userGroup_be;
        }
    }

    public static HashMap<Integer, List<Integer>> readOpenUserGroupMap(HTreeMap<String, String> userGroupMap){
        HashMap<Integer, List<Integer>> userGroup_be = new HashMap<>();
        for (Object uG : userGroupMap.keySet()) {
            if (!"isInitialized".equals(uG.toString())) {
                List<Integer> intList = new ArrayList<>();
                String str = userGroupMap.get(uG).replaceAll("[\\[\\]]", "");
                str = str.replaceAll(" ", "");
                List<String> strList = Arrays.asList(str.split(","));
                for (String s : strList) intList.add(Integer.valueOf(s));
                userGroup_be.put(Integer.valueOf(uG.toString()), intList);
            }
        }
        return userGroup_be;
    }

    public static HashMap<String, List<Integer>> readCredGroupMap(){
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> credGroupMap =
                     (HTreeMap<String, String>) db.hashMap("credGroupMap").createOrOpen()) {
            HashMap<String, List<Integer>> cred_userGroup = new HashMap<>();
            for (Object cred : credGroupMap.keySet()) {
                if (!"isInitialized".equals(cred.toString())) {
                    List<Integer> intList = new ArrayList<>();
                    String str = credGroupMap.get(cred).replaceAll("[\\[\\]]", "");
                    str = str.replaceAll(" ", "");
                    List<String> strList = Arrays.asList(str.split(","));
                    for (String s : strList) intList.add(Integer.valueOf(s));
                    cred_userGroup.put(cred.toString(), intList);
                }
            }
            return cred_userGroup;
        }
    }

    public static HashMap<String, List<Integer>> readOpenCredGroupMap(HTreeMap<String, String> credGroupMap){
        HashMap<String, List<Integer>> cred_userGroup = new HashMap<>();
        for (Object cred : credGroupMap.keySet()) {
            if (!"isInitialized".equals(cred.toString())) {
                List<Integer> intList = new ArrayList<>();
                String str = credGroupMap.get(cred).replaceAll("[\\[\\]]", "");
                str = str.replaceAll(" ", "");
                List<String> strList = Arrays.asList(str.split(","));
                for (String s : strList) intList.add(Integer.valueOf(s));
                cred_userGroup.put(cred.toString(), intList);
            }
        }
        return cred_userGroup;
    }

    public static HashMap<String, List<Integer>> readCredentialMap(){
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> credentialMap =
                     (HTreeMap<String, String>) db.hashMap("credentialMap").createOrOpen()) {

            HashMap<String, List<Integer>> credentials = new HashMap<>();
            for (Object cred : credentialMap.keySet()) {
                if (!"isInitialized".equals(cred.toString())) {
                    List<Integer> intList = new ArrayList<>();
                    String str = credentialMap.get(cred).replaceAll("[\\[\\]]", "");
                    str = str.replaceAll(" ", "");
                    List<String> strList = Arrays.asList(str.split(","));
                    for (String s : strList) intList.add(Integer.valueOf(s));
                    credentials.put(cred.toString(), intList);
                }
            }
            return credentials;
        }
    }

    public static HashMap<String, List<Integer>> openReadCredentialMap(HTreeMap<String, String> credentialMap){
        HashMap<String, List<Integer>> credentials = new HashMap<>();
        for (Object cred : credentialMap.keySet()) {
            if (!"isInitialized".equals(cred.toString())) {
                List<Integer> intList = new ArrayList<>();
                String str = credentialMap.get(cred).replaceAll("[\\[\\]]", "");
                str = str.replaceAll(" ", "");
                List<String> strList = Arrays.asList(str.split(","));
                for (String s : strList) intList.add(Integer.valueOf(s));
                credentials.put(cred.toString(), intList);
            }
        }
        return credentials;
    }

    public static HashMap<Integer, Attribute> readAttributesMap() {
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            HashMap<Integer, Attribute> attributes = new HashMap<>();
            for (Object attr : attributeMap.keySet()) {
                if (!"isInitialized".equals(attr.toString())) {
                    List<String> strList = Arrays.asList(attributeMap.get(attr).split(","));
                    Attribute attribute = new Attribute(strList.get(0), strList.get(1), strList.get(2));
                    attributes.put(Integer.parseInt(attr.toString()), attribute);
                }
            }
            return attributes;
        }
    }

    public static HashMap<Integer, Attribute> readOpenAttributesMap(HTreeMap<String, String> attributeMap) {
        HashMap<Integer, Attribute> attributes = new HashMap<>();
        for (Object attr : attributeMap.keySet()) {
            if (!"isInitialized".equals(attr.toString())) {
                List<String> strList = Arrays.asList(attributeMap.get(attr).split(","));
                Attribute attribute = new Attribute(strList.get(0), strList.get(1), strList.get(2));
                attributes.put(Integer.parseInt(attr.toString()), attribute);
            }
        }
        return attributes;
    }

    public static HashMap<String, String> readACPMap() {
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> acpMap =
                     (HTreeMap<String, String>) db.hashMap("acpMap").createOrOpen()) {

            HashMap<String, String> credentials = new HashMap<>();
            for (Object acp : acpMap.keySet()) {
                if (!"isInitialized".equals(acp.toString())) {
                    credentials.put(acp.toString(), acpMap.get(acp).toString());
                }
            }
            return credentials;
        }
    }

    public static HashMap<String, String> readOpenACPMap(HTreeMap<String, String> acpMap) {
        HashMap<String, String> credentials = new HashMap<>();
        for (Object acp : acpMap.keySet()) {
            if (!"isInitialized".equals(acp.toString())) {
                credentials.put(acp.toString(), acpMap.get(acp).toString());
            }
        }
        return credentials;
    }

}
