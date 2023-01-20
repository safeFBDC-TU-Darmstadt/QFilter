package common;

public enum Comparison {
    EQUALS("="), GREATER(">"), SMALLER("<"), GREATER_EQ(">="), SMALLER_EQ("<=");
    public final String token;

    Comparison(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}