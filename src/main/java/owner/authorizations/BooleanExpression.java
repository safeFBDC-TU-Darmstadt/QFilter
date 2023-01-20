package owner.authorizations;

import common.Attribute;
import common.Comparison;
import common.ConditionalType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Grammar:
 * Supported expressions: 'condition'
 * Supported expressions: 'condition AND condition'
 * Supported expressions: 'condition AND (condition OR condition)'
 * Supported expressions: 'condition OR condition'
 * Supported expressions: 'condition OR (condition AND condition)'
 * condition ::= {Z} <p>
 */
public class BooleanExpression {
    private int condition1;
    private int condition2;
    private int condition3;
    private int condition4;
    private Enum<ConditionalType> type;

    private Enum<ConditionalType> combinationType;

    private int combinationExpression;

    private List<Integer> applicable_attr1;

    private List<Integer> applicable_attr2;
    private List<Integer> applicable_attr3;
    private List<Integer> applicable_attr4;

    public BooleanExpression(int condition1, int condition2, String type, String combinationType) {
        this.condition1 = condition1;
        this.condition2 = condition2;
        this.condition3 = -1;
        this.condition4 = -1;
        switch(type){
            case "or": this.type= ConditionalType.DISJUNCTIVE; break;
            case "and": this.type= ConditionalType.CONJUNCTIVE; break;
            default:
                if(condition2<0){
                    this.type=ConditionalType.None;
                }else{
                    this.type=ConditionalType.CONJUNCTIVE;
                }break;
        }
        switch(combinationType){
            case "or": this.combinationType= ConditionalType.DISJUNCTIVE; break;
            case "and": this.combinationType= ConditionalType.CONJUNCTIVE; break;
            default:
                if(condition2<0){
                    this.combinationType=ConditionalType.None;
                }else{
                    this.combinationType=ConditionalType.CONJUNCTIVE;
                }break;
        }
    }
    public BooleanExpression(int condition1, int condition2, int condition3, int condition4, String type, String combinationType) {
        this.condition1 = condition1;
        this.condition2 = condition2;
        this.condition3 = condition3;
        this.condition4 = condition4;
        switch(type){
            case "or": this.type= ConditionalType.DISJUNCTIVE; break;
            case "and": this.type= ConditionalType.CONJUNCTIVE; break;
            default:
                if(condition2<0){
                    this.type=ConditionalType.None;
                }else{
                    this.type=ConditionalType.CONJUNCTIVE;
                }break;
        }
        switch(combinationType){
            case "or": this.combinationType= ConditionalType.DISJUNCTIVE; break;
            case "and": this.combinationType= ConditionalType.CONJUNCTIVE; break;
            default:
                if(condition2<0){
                    this.combinationType=ConditionalType.None;
                }else{
                    this.combinationType=ConditionalType.CONJUNCTIVE;
                }break;
        }
    }


    public Enum<ConditionalType> getCombinationType() {
        return combinationType;
    }

    public void setCombinationType(Enum<ConditionalType> combinationType) {
        this.combinationType = combinationType;
    }

    public int getCombinationExpression() {
        return combinationExpression;
    }

    public void setCombinationExpression(int combinationExpression) {
        this.combinationExpression = combinationExpression;
    }
    public List<Integer> getApplicable_attr1() {
        return applicable_attr1;
    }

    public List<Integer> getApplicable_attr2() {
        return applicable_attr2;
    }
    public void setApplicable_attr1(List<Integer> applicable_attr1) {
        this.applicable_attr1 = applicable_attr1;
    }

    public void setApplicable_attr2(List<Integer> applicable_attr2) {
        this.applicable_attr2 = applicable_attr2;
    }

    public int getCondition1() {
        return condition1;
    }

    public int getCondition2() {
        return condition2;
    }

    public Enum<ConditionalType> getType() {
        return type;
    }

    public void determine_applicableAttr1(HashMap<Integer, Attribute> attributes){
        List<Integer> applicableAttr = new ArrayList<>();
        if(attributes.get(this.condition1).getComparison() == Comparison.EQUALS){
            applicableAttr.add(this.condition1);
            this.applicable_attr1 = applicableAttr;
        }else{
            if(Float.isNaN(attributes.get(this.condition1).getNumValue())){
                applicableAttr.add(this.condition1);
                this.applicable_attr1 = applicableAttr;
            }else{
                if(attributes.get(this.condition1).getComparison().equals(Comparison.GREATER)){
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()>condition1 && (attributes.get(att).getComparison()==Comparison.GREATER ||
                                attributes.get(att).getComparison()==Comparison.GREATER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))
                                || (attributes.get(att).getNumValue()==condition1 &&
                                (attributes.get(att).getComparison()==Comparison.GREATER || attributes.get(att).getComparison()==Comparison.GREATER_EQ))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr1 = applicableAttr;
                }else if(attributes.get(this.condition1).getComparison().equals(Comparison.SMALLER)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()<condition1 && (attributes.get(att).getComparison()==Comparison.SMALLER ||
                                attributes.get(att).getComparison()==Comparison.SMALLER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))
                                || (attributes.get(att).getNumValue()==condition1 &&
                                (attributes.get(att).getComparison()==Comparison.SMALLER || attributes.get(att).getComparison()==Comparison.SMALLER_EQ))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr1 = applicableAttr;
                }else if(attributes.get(this.condition1).getComparison().equals(Comparison.GREATER_EQ)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()>=condition1 && (attributes.get(att).getComparison()==Comparison.GREATER ||
                                attributes.get(att).getComparison()==Comparison.GREATER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr1 = applicableAttr;
                }else if(attributes.get(this.condition1).getComparison().equals(Comparison.SMALLER_EQ)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()<=condition1 && (attributes.get(att).getComparison()==Comparison.SMALLER ||
                                attributes.get(att).getComparison()==Comparison.SMALLER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr1 = applicableAttr;
                }else {
                    applicableAttr.add(this.condition1);
                    this.applicable_attr1 = applicableAttr;
                }
            }
        }
    }

    public void determine_applicableAttr2(HashMap<Integer, Attribute> attributes){
        List<Integer> applicableAttr = new ArrayList<>();
        if(attributes.get(this.condition2).getComparison() == Comparison.EQUALS){
            applicableAttr.add(this.condition2);
            this.applicable_attr2 = applicableAttr;
        }else{
            if(Float.isNaN(attributes.get(this.condition2).getNumValue())){
                applicableAttr.add(this.condition2);
                this.applicable_attr2 = applicableAttr;
            }else{
                if(attributes.get(this.condition2).getComparison().equals(Comparison.GREATER)){
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()>condition2 && (attributes.get(att).getComparison()==Comparison.GREATER ||
                                attributes.get(att).getComparison()==Comparison.GREATER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))
                                || (attributes.get(att).getNumValue()==condition2 &&
                                (attributes.get(att).getComparison()==Comparison.GREATER || attributes.get(att).getComparison()==Comparison.GREATER_EQ))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr2 = applicableAttr;
                }else if(attributes.get(this.condition2).getComparison().equals(Comparison.SMALLER)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()<condition2 && (attributes.get(att).getComparison()==Comparison.SMALLER ||
                                attributes.get(att).getComparison()==Comparison.SMALLER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))
                                || (attributes.get(att).getNumValue()==condition2 &&
                                (attributes.get(att).getComparison()==Comparison.SMALLER || attributes.get(att).getComparison()==Comparison.SMALLER_EQ))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr2 = applicableAttr;
                }else if(attributes.get(this.condition2).getComparison().equals(Comparison.GREATER_EQ)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()>=condition2 && (attributes.get(att).getComparison()==Comparison.GREATER ||
                                attributes.get(att).getComparison()==Comparison.GREATER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr2 = applicableAttr;
                }else if(attributes.get(this.condition2).getComparison().equals(Comparison.SMALLER_EQ)) {
                    for(int att:attributes.keySet()){
                        if((attributes.get(att).getNumValue()<=condition2 && (attributes.get(att).getComparison()==Comparison.SMALLER ||
                                attributes.get(att).getComparison()==Comparison.SMALLER_EQ || attributes.get(att).getComparison()==Comparison.EQUALS))){
                            applicableAttr.add(att);
                        }
                    }
                    this.applicable_attr2 = applicableAttr;
                }else {
                    applicableAttr.add(this.condition2);
                    this.applicable_attr2 = applicableAttr;
                }
            }
        }
    }

    public boolean sameObjects(BooleanExpression be){
        return (this.type.name().equals(be.type.name()) && this.condition1==be.condition1 &&
                this.condition2==be.condition2 && this.combinationType==be.combinationType && this.getCombinationExpression()==be.combinationExpression);
    }

}