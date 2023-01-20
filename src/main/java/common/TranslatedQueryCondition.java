package common;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * (only "="-conditions allowed)
 */
public class TranslatedQueryCondition implements Serializable {
    @Serial
    private static final long serialVersionUID = 3503172886893875348L;
    private final String attributeName;
    private final List<Integer> valueShares;

    public TranslatedQueryCondition(String attributeName, List<Integer> valueShares) {
        this.attributeName = attributeName;
        this.valueShares = valueShares;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public List<Integer> getValueShares() {
        return valueShares;
    }
}
