package owner.authorizations;

import common.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.util.*;

import static owner.DBAccess.*;

public class CredentialManager {
    private HashMap<String, List<Integer>> credentials = new HashMap<>();

    private HashMap<Integer, Attribute> attributes = new HashMap<>();


    public CredentialManager() {
        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> credentialMap =
                     (HTreeMap<String, String>) db.hashMap("credentialMap").createOrOpen();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            Object isCredentialMapInitialized = credentialMap.get("isInitialized");
            if (isCredentialMapInitialized == null) {
                initializeCredentialMap(credentialMap);
            } else {
                credentials.putAll(openReadCredentialMap(credentialMap));
            }

            Object isAttributeMapInitialized = attributeMap.get("isInitialized");
            if (isAttributeMapInitialized == null) {
                initializeAttributeMap(attributeMap);
            } else {
                attributes.putAll(readOpenAttributesMap(attributeMap));
            }
        }
    }

    public HashMap<String, List<Integer>> getCredentials() {
        return credentials;
    }

    public HashMap<Integer, Attribute> getAttributes() {
        return attributes;
    }

    private void initializeCredentialMap(HTreeMap<String, String> credentialMap) {
        credentialMap.put("isInitialized", "true");
    }

    private void initializeAttributeMap(HTreeMap<String, String> attributeMap) {
        attributeMap.put("isInitialized", "true");
    }

    public String getUserCredentials(HashMap<String, List<String>> user_attr) {
        String token = UUID.randomUUID().toString();

        HashMap<String, List<Integer>> new_credentials = new HashMap<>();
        HashMap<Integer, Attribute> new_attributes = new HashMap<>();

        List<Integer> attr_list = new ArrayList<>();
        for (String key : user_attr.keySet()) {
            String comp = user_attr.get(key).get(0).strip();
            Attribute attr = new Attribute(key, comp, user_attr.get(key).get(1));
            int index = -1;
            for (int mapIndex : attributes.keySet()) {
                if (attributes.get(mapIndex).sameObjects(attr)) {
                    index = mapIndex;
                    break;
                }
            }
            if (index == -1) {
                index = attributes.size() + 1;
                attributes.put(index, attr);
                new_attributes.put(index, attr);
            }
            attr_list.add(index);
        }
        credentials.put(token, attr_list);
        new_credentials.put(token, attr_list);

        try (DB db = DBMaker.fileDB("src/main/resources/owner.db").make();
             HTreeMap<String, String> credentialMap =
                     (HTreeMap<String, String>) db.hashMap("credentialMap").createOrOpen();
             HTreeMap<String, String> attributeMap =
                     (HTreeMap<String, String>) db.hashMap("attributeMap").createOrOpen()) {

            for (String cred : new_credentials.keySet()) {
                credentialMap.put(cred, String.join(",", new_credentials.get(cred).toString()));
            }
            for (int index : new_attributes.keySet()) {
                Attribute attr = new_attributes.get(index);
                Comparison comp = (Comparison) attr.getComparison();
                String attr_val = attr.getName() + "," + comp.getToken() + "," + attr.getValue();
                attributeMap.put(String.valueOf(index), attr_val);
            }
        }

        assignUserToGroup(new_credentials);

        return token;
    }


    private void assignUserToGroup(HashMap<String, List<Integer>> new_credentials) {
        HashMap<Integer, List<Integer>> current_groups = readUserGroupMap();
        HashMap<Integer, BooleanExpression> booleanExpressions = readBoolExpMap();
        HashMap<String, List<Integer>> cred_userGroup = readCredGroupMap();

        if (current_groups.isEmpty() || booleanExpressions.isEmpty()) {
            System.out.println("Warning: No groups attached because none exists yet.");
        }
        for (int group : current_groups.keySet()) {
            List<BooleanExpression> all_be = new ArrayList<>();
            for (int idx : current_groups.get(group)) {
                BooleanExpression be = booleanExpressions.get(idx);
                all_be.add(be);
            }

            for (String cred : new_credentials.keySet()) {
                // Get all Users that have valid attributes
                boolean cred_found = false;
                boolean subresult_cred = true;
                boolean all_conjunctive = true;
                for (int be_idx = 0; be_idx < all_be.size(); be_idx++) {
                    if (all_be.get(be_idx).getType() != ConditionalType.CONJUNCTIVE) {
                        all_conjunctive = false;
                    }
                }
                if (all_conjunctive) {
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
                } else {
                    // Case: BEs are combined to one "or" condition
                    List<BooleanExpression> disj_cond = new ArrayList<>();
                    List<BooleanExpression> conj_cond = new ArrayList<>();
                    for (int be_idx = 0; be_idx < all_be.size(); be_idx++) {
                        if (all_be.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                            conj_cond.add(all_be.get(be_idx));
                        } else {
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
                    if (subresult_cred) {
                        Collections.sort(disj_cond, Comparator.comparingInt(BooleanExpression::getCombinationExpression));
                        for (int be_idx = 0; be_idx < conj_cond.size(); be_idx = +2) {
                            if (disj_cond.get(be_idx).getType() == ConditionalType.CONJUNCTIVE) {
                                if (!((credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr1()) && credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr2())) ||
                                        (credentials.get(cred).containsAll(disj_cond.get(be_idx + 1).getApplicable_attr1()) && credentials.get(cred).containsAll(disj_cond.get(be_idx + 1).getApplicable_attr2())))) {
                                    subresult_cred = false;
                                    break;
                                }
                            } else {
                                if (!(credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr1()) || credentials.get(cred).containsAll(disj_cond.get(be_idx).getApplicable_attr2())) ||
                                        (credentials.get(cred).containsAll(disj_cond.get(be_idx + 1).getApplicable_attr1()) || credentials.get(cred).containsAll(disj_cond.get(be_idx + 1).getApplicable_attr2()))) {
                                    subresult_cred = false;
                                    break;
                                }
                            }
                        }
                    }

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
    }
}
