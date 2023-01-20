import user.exceptions.QueryParsingException;
import user.queryparsing.ParsedQuery;
import user.queryparsing.QueryParser;
import user.queryparsing.QueryCondition;
import common.AggregateType;
import common.ConditionalType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class QueryParserTests {

    @Test
    void validQuery1() throws QueryParsingException {
        String queryString = "select count(*) from table;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.COUNT, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertNull(query.getConditionalType());
        assertEquals(0, query.getConditions().size());
    }

    @Test
    void validQuery2() throws QueryParsingException {
        String queryString = "seLecT   cOUnt( * ) FROM  \t table\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.COUNT, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertNull(query.getConditionalType());
        assertEquals(0, query.getConditions().size());
    }

    @Test
    void validQuery3() throws QueryParsingException {
        String queryString = "select sum(*) from table where x=y  ;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.SUM, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertNull(query.getConditionalType());
        assertEquals(1, query.getConditions().size());
        List<QueryCondition> xCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("x")).toList();
        assertEquals(1, xCond.size());
        assertEquals("y", xCond.get(0).getValue());
    }

    @Test
    void validQuery4() throws QueryParsingException {
        String queryString = "select avg(*) from table where \t\ta=\tb \tAnD c=d\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.AVG, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertEquals(ConditionalType.CONJUNCTIVE, query.getConditionalType());
        assertEquals(2, query.getConditions().size());
        List<QueryCondition> aCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("a")).toList();
        assertEquals(1, aCond.size());
        assertEquals("b", aCond.get(0).getValue());
        List<QueryCondition> cCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("c")).toList();
        assertEquals(1, cCond.size());
        assertEquals("d", cCond.get(0).getValue());
    }

    @Test
    void validQuery5() throws QueryParsingException {
        String queryString = "select avg(*) from table where \t\tandroid=x \tOr orbit=y\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.AVG, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertEquals(ConditionalType.DISJUNCTIVE, query.getConditionalType());
        assertEquals(2, query.getConditions().size());
        List<QueryCondition> androidCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("android")).toList();
        assertEquals(1, androidCond.size());
        assertEquals("x", androidCond.get(0).getValue());
        List<QueryCondition> orbitCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("orbit")).toList();
        assertEquals(1, orbitCond.size());
        assertEquals("y", orbitCond.get(0).getValue());
    }

    @Test
    void validQuery6() throws QueryParsingException {
        String queryString = "select avg(*) from table where \t\tandroid=orbit\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.AVG, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertNull(query.getConditionalType());
        assertEquals(1, query.getConditions().size());
        List<QueryCondition> androidCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("android")).toList();
        assertEquals(1, androidCond.size());
        assertEquals("orbit", androidCond.get(0).getValue());
    }

    @Test
    void validQuery7() throws QueryParsingException {
        String queryString = "select avg(*) from table where \t\tx='moon lander' \tOr y='translators'\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.AVG, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertEquals(ConditionalType.DISJUNCTIVE, query.getConditionalType());
        assertEquals(2, query.getConditions().size());
        List<QueryCondition> xCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("x")).toList();
        assertEquals(1, xCond.size());
        assertEquals("'moon lander'", xCond.get(0).getValue());
        List<QueryCondition> yCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("y")).toList();
        assertEquals(1, yCond.size());
        assertEquals("'translators'", yCond.get(0).getValue());
    }

    @Test
    void validQuery8() throws QueryParsingException {
        String queryString = "select avg(*) from table where \t\tx='moon l and er' \tOr y='translat or s'\t;";
        ParsedQuery query = QueryParser.parseQuery(queryString);
        assertEquals(AggregateType.AVG, query.getAggregateType());
        assertEquals("*", query.getAttribute());
        assertEquals("table", query.getTable());
        assertEquals(ConditionalType.DISJUNCTIVE, query.getConditionalType());
        assertEquals(2, query.getConditions().size());
        List<QueryCondition> xCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("x")).toList();
        assertEquals(1, xCond.size());
        assertEquals("'moon l and er'", xCond.get(0).getValue());
        List<QueryCondition> yCond = query.getConditions().stream().filter(queryCondition -> queryCondition.getAttributeName().equalsIgnoreCase("y")).toList();
        assertEquals(1, yCond.size());
        assertEquals("'translat or s'", yCond.get(0).getValue());
    }

    @Test
    void invalidQueries() {
        String[] queries = new String[]{"selct attribute from table;",
                                        "sel ect attribute from table;",
                                        "select av g(attribute) from table;",
                                        "select vg(attribute) from table;",
                                        "select avg(attribute) f rom table;",
                                        "select avg(attribute) fro table;",
                                        "select avg(attribute) from table wh ere x=y;",
                                        "select avg(attribute) from table wh x=y;",
                                        "select avg(attribute) from table where x=y ad a=b;",
                                        "select attribute from table;",
                                        "select attribute from table where x=y;",
                                        "select avg(attribute) from table where x<y;",
                                        "select avg(attribute) from table where x<y and a=b;",
                                        "select avg(attribute) from table where x=y and a<b;",
                                        "select avg(2*(1+attribute)) from table where x=y and a=b;",
                                        "select avg(attribute) from table where (x=y) and a=b;"};
        for (String query : queries)
            assertThrows(QueryParsingException.class, () -> QueryParser.parseQuery(query));
    }

}
