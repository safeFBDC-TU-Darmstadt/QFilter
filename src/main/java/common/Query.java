package common;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Supported queries: 'select aggregate(attribute) from table [where cond];'<p>
 * cond ::= eqExpr | eqExpr and cond | eqExpr or cond<p>
 * eqExpr ::= attribute=value
 */
public class Query implements Serializable {

    @Serial
    private static final long serialVersionUID = -7281682758369073861L;

    private AggregateType aggregateType;
    private String attribute;
    private String table;
    private @Nullable ConditionalType conditionalType;
    /**
     * Maps attribute names to a value / list of values they should equal to.
     */
    private List<TranslatedQueryCondition> conditions;
    private int serverIdx;

    public Query(AggregateType aggregateType, String attribute, String table,
                 @Nullable ConditionalType conditionalType, List<TranslatedQueryCondition> conditions) {
        this.aggregateType = aggregateType;
        this.attribute = attribute;
        this.table = table;
        this.conditionalType = conditionalType;
        this.conditions = conditions;
    }

    public Query(AggregateType aggregateType, String attribute, String table, @Nullable ConditionalType conditionalType,
                 List<TranslatedQueryCondition> conditions, int serverIdx) {
        this.aggregateType = aggregateType;
        this.attribute = attribute;
        this.table = table;
        this.conditionalType = conditionalType;
        this.conditions = conditions;
        this.serverIdx = serverIdx;
    }

    public AggregateType getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(AggregateType aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public @Nullable ConditionalType getConditionalType() {
        return conditionalType;
    }

    public void setConditionalType(@Nullable ConditionalType conditionalType) {
        this.conditionalType = conditionalType;
    }

    public List<TranslatedQueryCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<TranslatedQueryCondition> conditions) {
        this.conditions = conditions;
    }

    public int getServerIdx() {
        return serverIdx;
    }

    public void setServerIdx(int serverIdx) {
        this.serverIdx = serverIdx;
    }
}
