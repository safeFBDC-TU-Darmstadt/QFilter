package common;

public enum ConditionalType {
    CONJUNCTIVE("and"), DISJUNCTIVE("or"), None("none");

    public final String token;

    ConditionalType(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }
}
