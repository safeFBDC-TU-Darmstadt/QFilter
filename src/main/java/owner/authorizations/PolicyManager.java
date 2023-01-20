package owner.authorizations;

import common.Attribute;
import common.ConditionalType;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import owner.exceptions.ExpressionParsingException;
import owner.exceptions.PolicyManagerException;

import java.util.*;

import static owner.DBAccess.*;

public class PolicyManager {
    private HashMap<Integer, BooleanExpression> booleanExpressions = new HashMap<>();
    private HashMap<Integer, List<Integer>> userGroup_be = new HashMap<>();
    private HashMap<String, List<Integer>> cred_userGroup = new HashMap<>();

    private HashMap<String, String> acps = new HashMap<>();


    public PolicyManager(){
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> boolExpMap =
                     (HTreeMap<String, String>) db.hashMap("boolExpMap").createOrOpen();
             HTreeMap<String, String> userGroupMap =
                     (HTreeMap<String, String>) db.hashMap("userGroupMap").createOrOpen();
             HTreeMap<String, String> credGroupMap =
                     (HTreeMap<String, String>) db.hashMap("credGroupMap").createOrOpen();
             HTreeMap<String, String> acpMap =
                     (HTreeMap<String, String>) db.hashMap("acpMap").createOrOpen()) {

            Object isBoolExpMapInitialized = boolExpMap.get("isInitialized");
            if (isBoolExpMapInitialized == null) {
                initializeBoolExpMap(boolExpMap);
            }else{
                booleanExpressions.putAll(readOpenBoolExpMap(boolExpMap));
            }

            Object isUserGroupMapInitialized = userGroupMap.get("isInitialized");
            if (isUserGroupMapInitialized == null){
                initializeUserGroupMap(userGroupMap);
            }else{
                userGroup_be.putAll(readOpenUserGroupMap(userGroupMap));
            }

            Object isACPMapInitialized = acpMap.get("isInitialized");
            if (isACPMapInitialized == null){
                initializeACPMap(acpMap);
            }else{
                acps.putAll(readOpenACPMap(acpMap));
            }

            Object isCredGroupMapInitialized = credGroupMap.get("isInitialized");
            if (isCredGroupMapInitialized == null){
                initializeCredGroupMap(credGroupMap);
            }else{
                cred_userGroup.putAll(readOpenCredGroupMap(credGroupMap));
            }
        }
    }

    private void initializeACPMap(HTreeMap<String, String> acpMap) {
        acpMap.put("isInitialized", "true");
    }

    private List<String> readAvailableAttributes(HTreeMap<String, String> attributeMap) {
        List<String> availableAttributes = new ArrayList<>();
        for(Object attr: attributeMap.keySet()){
            if(!"isInitialized".equals(attr.toString())) {
                availableAttributes.add(attr.toString());
            }
        }
        return availableAttributes;
    }


    public HashMap<Integer, List<Integer>>  createBooleanExpression(String bool_expr) throws ExpressionParsingException, PolicyManagerException {
        List<String> availableAttributes = new ArrayList<>();
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> boolExpMap =
                     (HTreeMap<String, String>) db.hashMap("boolExpMap").createOrOpen();
             HTreeMap<String, String> userGroupMap =
                     (HTreeMap<String, String>) db.hashMap("userGroupMap").createOrOpen();
             //HTreeMap<String, String> credGroupMap =
             //        (HTreeMap<String, String>) db.hashMap("credGroupMap").createOrOpen();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            Object isattributeMapInitialized = attributeMap.get("isInitialized");
            if (isattributeMapInitialized == null) {
                throw new PolicyManagerException("No attributes defined");
            }
            availableAttributes = readAvailableAttributes(attributeMap);
            cred_userGroup.putAll(readOpenCredGroupMap(userGroupMap));
        }

        // Validation if all attributes are available
        String str = bool_expr.replaceAll("\\D+"," ").replaceAll("\\s+", ",");
        List<String> attr = new ArrayList<String>(Arrays.asList(str.strip().split(",")));
        attr.removeIf(item -> item == null || "".equals(item));
        if(availableAttributes.containsAll(attr))
        {
            // Create Boolean Expression
            List<BooleanExpression> disj_be = ExpressionParser.parseExpression(bool_expr);
            HashMap<Integer, BooleanExpression> new_bes = new HashMap<>();
            List<Integer> disj_be_int = new ArrayList<>();
            int first_expression = -1;
            for(BooleanExpression be: disj_be){
                int index = -1;
                for(int ind: booleanExpressions.keySet()){
                    if(booleanExpressions.get(ind).sameObjects(be)){
                        index = ind;
                        break;
                    }
                }
                if(index==-1){
                    index = booleanExpressions.size()+1;
                    first_expression = index;
                    be.setCombinationExpression(first_expression);
                    booleanExpressions.put(index, be);
                    new_bes.put(index, be);
                }
                disj_be_int.add(index);
            }

            // Create User Groups
            HashMap<Integer, List<Integer>> new_groups = new HashMap<>();
            for(int be: disj_be_int){
                userGroup_be.put(userGroup_be.size()+1, Collections.singletonList(be));
                new_groups.put(userGroup_be.size()+1, Collections.singletonList(be));
            }
            userGroup_be.put(userGroup_be.size()+1, disj_be_int);
            new_groups.put(userGroup_be.size()+1, disj_be_int);

            // Assign User Groups
            assignGroupsToUser(new_groups);

            try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
                 HTreeMap<String, String> boolExpMap =
                         (HTreeMap<String, String>) db.hashMap("boolExpMap").createOrOpen();
                 HTreeMap<String, String> userGroupMap =
                         (HTreeMap<String, String>) db.hashMap("userGroupMap").createOrOpen()) {

                // Write in DB
                for (int group : new_groups.keySet()) {
                    userGroupMap.put(String.valueOf(group), String.join(",", new_groups.get(group).toString()));
                }
                for (int index : new_bes.keySet()) {
                    BooleanExpression be = new_bes.get(index);
                    ConditionalType type = (ConditionalType) be.getType();
                    ConditionalType combinationType = (ConditionalType) be.getCombinationType();
                    String be_val = be.getCondition1() + "," + be.getCondition2() +"," + type.getToken() + "," +
                            String.join("|", be.getApplicable_attr1().toString()).replace("[", "").replace("]", "") + "," +
                            String.join("|", be.getApplicable_attr2().toString()).replace("[", "").replace("]", "")  + "," +
                            combinationType.getToken() + "," + be.getCombinationExpression();
                    boolExpMap.put(String.valueOf(boolExpMap.size() + 1), be_val);
                }
                return new_groups;
            }
        }else{
            throw new PolicyManagerException("Incorrect or no Attributes defined");
        }
    }

    // String Condition: if tuple based: primary Key, if attribute base: column name
    public void createACPs(HashMap<Integer, List<Integer>> be_group, boolean tupleBased, String condition, int condition_type, boolean permission) throws PolicyManagerException{
        if(!(condition_type<=3 && condition_type>=-1)){
            throw new PolicyManagerException("Incorrect condition_type chosen");
        }
        if(!userGroup_be.keySet().containsAll(be_group.keySet())){
        //if(userGroup_be.get(be_group) == null) {
            throw new PolicyManagerException("Incorrect be_group(s) chosen");
        }
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> acpMap =
                     (HTreeMap<String, String>) db.hashMap("acpMap").createOrOpen()) {
            String tuple = tupleBased ? "1" : "0";
            String perm = permission ? "1" : "0";
            List<String> intList = new ArrayList<>();
            for (Integer i : be_group.keySet()) {
                intList.add(String.valueOf(i));
            }
            String acp = String.join(",", intList) + "|" + tuple + "|"  + condition_type + "|"  + condition + "|"  + perm;
            acpMap.put(String.valueOf(acpMap.size()), acp);
        }
    }

    private void assignGroupsToUser(HashMap<Integer, List<Integer>> new_groups) {
        // Get BooleanExpressions
        for (int group : new_groups.keySet()) {

            // Get all attributes that are valid for boolean expression
            HashMap<Integer, Attribute> attributes = getCurrentAttributes();

            List<BooleanExpression> all_be = new ArrayList<>();
            for (int idx : new_groups.get(group)) {
                BooleanExpression be = booleanExpressions.get(idx);
                all_be.add(be);
                be.determine_applicableAttr1(attributes);
                be.determine_applicableAttr2(attributes);
            }

            HashMap<String, List<Integer>> credentials = getCurrentUsers();
            for (String cred : credentials.keySet()) {
                // Get all Users that have valid attributes
                boolean cred_found = false;
                boolean subresult_cred = true;
                boolean all_conjunctive=true;
                for (int be_idx = 0; be_idx < all_be.size(); be_idx++) {
                    if (all_be.get(be_idx).getType() != ConditionalType.CONJUNCTIVE) {
                        all_conjunctive = false;
                    }
                }
                if(all_conjunctive){
                    for (int be_idx = 0; be_idx < all_be.size(); be_idx++) {
                        if (all_be.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                            // Alternative: credentials.get(cred).containsAll(all_be.get(0).getApplicable_attr1()) ?> for greater/smaller comparisons
                            // credentials.get(cred).contains(all_be.get(be_idx).getCondition1()
                            if (!(credentials.get(cred).containsAll(all_be.get(be_idx).getApplicable_attr1()) && credentials.get(cred).containsAll(all_be.get(be_idx).getApplicable_attr2()))) {
                                subresult_cred = false;
                                break;
                            }
                        } else {
                            if (!(credentials.get(cred).containsAll(all_be.get(be_idx).getApplicable_attr1()) || credentials.get(cred).containsAll(all_be.get(be_idx).getApplicable_attr2()))) {
                                subresult_cred = false;
                                break;
                            }
                        }
                    }
                }else{
                    // Fall: 2 BEs werden kombiniert zu einer "or" condition, alle
                    List<BooleanExpression> disj_cond = new ArrayList<>();
                    List<BooleanExpression> conj_cond = new ArrayList<>();
                    for (int be_idx = 0; be_idx < all_be.size(); be_idx++) {
                        if (all_be.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                            conj_cond.add(all_be.get(be_idx));
                        }else{
                            disj_cond.add(all_be.get(be_idx));
                        }
                    }
                    for (int be_idx = 0; be_idx < conj_cond.size(); be_idx++) {
                        if (conj_cond.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                            // Alternative: credentials.get(cred).containsAll(all_be.get(0).getApplicable_attr1()) ?> for greater/smaller comparisons
                            // credentials.get(cred).contains(all_be.get(be_idx).getCondition1()
                            if (!(credentials.get(cred).containsAll(conj_cond.get(be_idx).getApplicable_attr1()) && credentials.get(cred).containsAll(conj_cond.get(be_idx).getApplicable_attr2()))) {
                                subresult_cred = false;
                                break;
                            }
                        } else {
                            if (!(credentials.get(cred).containsAll(conj_cond.get(be_idx).getApplicable_attr1()) || credentials.get(cred).containsAll(conj_cond.get(be_idx).getApplicable_attr2()))) {
                                subresult_cred = false;
                                break;
                            }
                        }
                    }
                    if(subresult_cred){
                        Collections.sort(disj_cond, Comparator.comparingInt(BooleanExpression::getCombinationExpression));
                        for (int be_idx = 0; be_idx < conj_cond.size(); be_idx=+2) {
                            if (disj_cond.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                                if (!((credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr1()) && credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr2()))||
                                        (credentials.get(cred).containsAll(disj_cond.get(be_idx+1).getApplicable_attr1()) && credentials.get(cred).containsAll(disj_cond.get(be_idx+1).getApplicable_attr2())))) {
                                    subresult_cred = false;
                                    break;
                                }
                            } else {
                                if (!(credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr1()) || credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr2()))||
                                        (credentials.get(cred).containsAll(disj_cond.get(be_idx+1).getApplicable_attr1()) || credentials.get(cred).containsAll(disj_cond.get(be_idx+1).getApplicable_attr2()))) {
                                    subresult_cred = false;
                                    break;
                                }
                            }
                        }
                    }
                }
                /*if(all_be.size()==1){
                    if(all_be.get(0).getType()==ConditionalType.CONJUNCTIVE){
                        // Alternative: credentials.get(cred).containsAll(all_be.get(0).getApplicable_attr1()) ?> for greater/smaller comparisons
                        if(credentials.get(cred).contains(all_be.get(0).getCondition1()) && credentials.get(cred).contains(all_be.get(0).getCondition2())){
                            cred_found=true;
                        }
                    }else{
                        if(credentials.get(cred).contains(all_be.get(0).getCondition1()) || credentials.get(cred).contains(all_be.get(0).getCondition2())){
                            cred_found=true;
                        }
                    }
                }else if(all_be.size()==2){
                    if(all_be.get(0).getType()==ConditionalType.CONJUNCTIVE && all_be.get(1).getType()==ConditionalType.CONJUNCTIVE){
                        if(credentials.get(cred).contains(all_be.get(0).getCondition1()) && credentials.get(cred).contains(all_be.get(0).getCondition2())
                                && credentials.get(cred).contains(all_be.get(1).getCondition1()) && credentials.get(cred).contains(all_be.get(1).getCondition2())){
                            cred_found=true;
                        }
                    }else if(all_be.get(0).getType()==ConditionalType.DISJUNCTIVE && all_be.get(1).getType()==ConditionalType.CONJUNCTIVE){
                        if((credentials.get(cred).contains(all_be.get(0).getCondition1()) || credentials.get(cred).contains(all_be.get(0).getCondition2()))
                                && credentials.get(cred).contains(all_be.get(1).getCondition1()) && credentials.get(cred).contains(all_be.get(1).getCondition2())){
                            cred_found=true;
                        }
                    }else if(all_be.get(0).getType()==ConditionalType.CONJUNCTIVE && all_be.get(1).getType()==ConditionalType.DISJUNCTIVE){
                        if(credentials.get(cred).contains(all_be.get(0).getCondition1()) && credentials.get(cred).contains(all_be.get(0).getCondition2())
                                && (credentials.get(cred).contains(all_be.get(1).getCondition1()) || credentials.get(cred).contains(all_be.get(1).getCondition2()))){
                            cred_found=true;
                        }
                    }else if(all_be.get(0).getType()==ConditionalType.DISJUNCTIVE && all_be.get(1).getType()==ConditionalType.DISJUNCTIVE){
                        if((credentials.get(cred).contains(all_be.get(0).getCondition1()) || credentials.get(cred).contains(all_be.get(0).getCondition2()))
                                && (credentials.get(cred).contains(all_be.get(1).getCondition1()) || credentials.get(cred).contains(all_be.get(1).getCondition2()))){
                            cred_found=true;
                        }
                    }
                }*/

                // Store user credentials with new groups
                if (subresult_cred) {
                    try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
                         HTreeMap<String, String> credGroupMap =
                                 (HTreeMap<String, String>) db.hashMap("credGroupMap").createOrOpen()) {


                        if (cred_userGroup.get(cred) == null) {
                            List<Integer> list = new ArrayList<>();
                            list.add(group);
                            cred_userGroup.put(cred, list);
                            credGroupMap.put(cred, String.join(",", list.toString()));
                        } else {
                            List<Integer> list = cred_userGroup.get(cred);
                            list.add(group);
                            cred_userGroup.put(cred, list);
                            credGroupMap.put(cred, String.join(",", list.toString()));
                        }
                        break;
                    }

                }
            }
        }
    }

    private HashMap<String, List<Integer>> getCurrentUsers() {
        HashMap<String, List<Integer>> credentials = new HashMap<>();
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> credentialMap =
                     (HTreeMap<String, String>) db.hashMap("credentialMap").createOrOpen()) {

            Object isCredentialMapInitialized = credentialMap.get("isInitialized");
            if (isCredentialMapInitialized != null) {
                for(Object cred: credentialMap.keySet()){
                    if(!"isInitialized".equals(cred.toString())) {
                        List<Integer> intList = new ArrayList<>();
                        String str = credentialMap.get(cred).replaceAll("[\\[\\]]", "");
                        str = str.replaceAll(" ", "");
                        List<String> strList = Arrays.asList(str.split(","));
                        for(String s : strList) intList.add(Integer.valueOf(s));
                        credentials.put(cred.toString(), intList);
                    }
                }
            }
        }
        return credentials;
    }

    private HashMap<Integer, Attribute> getCurrentAttributes() {
        HashMap<Integer, Attribute> attributes = new HashMap<>();
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            Object isAttributeMapInitialized = attributeMap.get("isInitialized");
            if (isAttributeMapInitialized != null) {
                for(Object attr: attributeMap.keySet()){
                    if(!"isInitialized".equals(attr.toString())) {
                        List<String> strList = Arrays.asList(attributeMap.get(attr).split(","));
                        Attribute attribute = new Attribute(strList.get(0), strList.get(1), strList.get(2));
                        attributes.put(Integer.parseInt(attr.toString()), attribute);
                    }
                }
            }
        }
        return attributes;
    }

    private void initializeBoolExpMap(HTreeMap<String, String> boolExpMap) {
        boolExpMap.put("isInitialized", "true");
    }
    private void initializeCredGroupMap(HTreeMap<String, String> credGroupMap) {
        credGroupMap.put("isInitialized", "true");
    }
    private void initializeUserGroupMap(HTreeMap<String, String> userGroupMap) {
        userGroupMap.put("isInitialized", "true");
    }


    public HashMap<Integer, BooleanExpression> getBooleanExpressions() {
        return booleanExpressions;
    }

    public HashMap<Integer, List<Integer>> getUserGroup_be() {
        return userGroup_be;
    }

    public HashMap<String, List<Integer>> getCred_userGroup() {
        cred_userGroup.putAll(readCredGroupMap());
        return cred_userGroup;
    }

}
