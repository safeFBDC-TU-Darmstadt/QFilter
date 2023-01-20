package common;

public enum AggregateType {
    COUNT("count"), SUM("sum"), AVG("avg");

    public final String token;

    AggregateType(String token) {
        this.token = token;
    }
}
