package user.queryparsing;

/**
 * (only "="-conditions allowed)
 */
public class QueryCondition {
    private final String attributeName;
    private final String value;

    public QueryCondition(String attributeName, String valueShares) {
        this.attributeName = attributeName;
        this.value = valueShares;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public String getValue() {
        return value;
    }
}
