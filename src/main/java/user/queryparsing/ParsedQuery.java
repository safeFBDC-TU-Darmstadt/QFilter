package user.queryparsing;

import common.AggregateType;
import common.ConditionalType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Supported queries: 'select aggregate(attribute) from table [where cond];'<p>
 * cond ::= eqExpr | eqExpr and cond | eqExpr or cond<p>
 * eqExpr ::= attribute=value
 */
public class ParsedQuery {
    private AggregateType aggregateType;
    private String attribute;
    private String table;
    private @Nullable ConditionalType conditionalType;
    /**
     * Maps attribute names to a value string they should equal to.
     */
    private List<QueryCondition> conditions = new ArrayList<>();

    public ParsedQuery() {
    }

    public ParsedQuery(ParsedQuery query) {
        this.aggregateType = query.aggregateType;
        this.attribute = query.attribute;
        this.table = query.table;
        this.conditionalType = query.conditionalType;
        this.conditions = new ArrayList<>(query.conditions);
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

    public List<QueryCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<QueryCondition> conditions) {
        this.conditions = conditions;
    }
}
