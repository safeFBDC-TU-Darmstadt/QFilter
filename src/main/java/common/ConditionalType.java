package common;

public enum ConditionalType {
    CONJUNCTIVE("and"), DISJUNCTIVE("or");

    public final String token;

    ConditionalType(String token) {
        this.token = token;
    }
}
