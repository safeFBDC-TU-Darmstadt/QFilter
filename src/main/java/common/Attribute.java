package common;

import common.*;

import java.io.Serializable;

public class Attribute implements Serializable {
    private String name;
    private Enum<Comparison> comparison;
    private String value;
    private float numValue;

    public Attribute(String name, String comp, String value) {
        this.name = name;
        switch(comp){
            case ">": this.comparison= Comparison.GREATER; break;
            case "<": this.comparison= Comparison.SMALLER; break;
            case ">=": this.comparison= Comparison.GREATER_EQ; break;
            case "<=": this.comparison= Comparison.SMALLER_EQ; break;
            default:
                this.comparison= Comparison.EQUALS; break;
        }
        this.value = value;
        try{
            this.numValue = Float.valueOf(numValue);
        } catch (Exception e) {}
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Enum<Comparison> getComparison() {
        return comparison;
    }

    public void setComparison(Enum<Comparison> comparison) {
        this.comparison = comparison;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        try{
            this.numValue = Float.valueOf(numValue);
        } catch (Exception e) {}
    }

    public float getNumValue() {
        return numValue;
    }

    public boolean sameObjects(Attribute attr){
        return (this.name.equals(attr.name) && this.value==attr.value &&
                this.comparison.equals(attr.comparison) && this.numValue==attr.numValue);
    }
}