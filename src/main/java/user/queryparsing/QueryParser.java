package user.queryparsing;

import user.exceptions.QueryParsingException;
import common.Query;
import common.AggregateType;
import common.ConditionalType;

import java.util.Map;

/**
 * Supported queries: 'select aggregate(attribute) from table [where cond];'<p>
 * cond ::= eqExpr | eqExpr and cond | eqExpr or cond<p>
 * eqExpr ::= attribute=int | attribute='string'
 */
public abstract class QueryParser {

    /**
     * Parses a supported query to a {@link ParsedQuery} object for easier handling in the following steps.
     * <p><p>
     * Supported queries: 'select aggregate(attribute) from table [where cond];'<p>
     * cond ::= eqExpr | eqExpr and cond | eqExpr or cond<p>
     * eqExpr ::= attribute=int | attribute='string'
     *
     * @param queryString the query string.
     * @return a {@link ParsedQuery} object representing the given query.
     * @throws QueryParsingException if the query syntax is invalid or the query is unsupported.
     */
    public static ParsedQuery parseQuery(String queryString) throws QueryParsingException {
        ParsedQuery query = new ParsedQuery();

        queryString = queryString.strip();

        if (!queryString.endsWith(";")) throw new QueryParsingException("Invalid query syntax. Missing ';'.");
        if (queryString.indexOf("(") != queryString.lastIndexOf("("))
            throw new QueryParsingException("Only one pair of brackets in for the aggregation allowed.");

        String remainingQueryString = setQueryAggregateType(queryString, query);
        remainingQueryString = setQueryAggregateAttribute(remainingQueryString, query);
        remainingQueryString = setQueryTable(remainingQueryString, query);
        setQueryConditions(remainingQueryString, query);

        return query;
    }

    /**
     * Sets the {@link AggregateType} of the passed {@link Query} object.
     *
     * @param strippedQueryString the stripped query string.
     * @param query               a {@link ParsedQuery} object representing the query string.
     * @return the substring of the given query string after the aggregate token.
     * @throws QueryParsingException if the query syntax is invalid or the query is unsupported.
     */
    private static String setQueryAggregateType(String strippedQueryString, ParsedQuery query) throws QueryParsingException {
        String select = "select";
        if (!strippedQueryString.toLowerCase().startsWith(select)) {
            throw new QueryParsingException("Invalid query syntax. Missing keyword 'select'.");
        }
        String subQuery = strippedQueryString.substring(select.length());

        // check if there is a whitespace after 'select'
        subQuery = checkLeadingWhitespaceAndStrip(subQuery, "Invalid query syntax. No whitespace after 'select'.");
        if (subQuery.isEmpty()) throw new QueryParsingException("Invalid query syntax. Missing aggregation part.");

        // check if the query has a supported aggregate function and set it in the query string (if applicable)
        for (AggregateType aggregateType : AggregateType.values()) {
            if (subQuery.toLowerCase().startsWith(aggregateType.token)) {
                query.setAggregateType(aggregateType);
                return subQuery.substring(aggregateType.token.length()).stripLeading();
            }
        }

        throw new QueryParsingException("Unsupported query. Only aggregate queries 'count', 'sum' and 'avg' supported.");
    }

    /**
     * Sets the attribute {@link String} of the passed {@link ParsedQuery} object.
     *
     * @param remainingQueryString the stripped substring of the full query string after the aggregate token.
     * @param query                a {@link ParsedQuery} object representing the query string.
     * @return the substring of the full query string after the bracket enclosing the aggregate attribute (not stripped!).
     * @throws QueryParsingException if the query syntax is invalid.
     */
    private static String setQueryAggregateAttribute(String remainingQueryString, ParsedQuery query) throws QueryParsingException {
        if (!remainingQueryString.startsWith("("))
            throw new QueryParsingException("Invalid query syntax. No '(' after aggregation function.");

        // has to be the correct closing bracket because only one pair is allowed in the query
        int closingBracketIdx = remainingQueryString.indexOf(')');

        if (closingBracketIdx == -1)
            throw new QueryParsingException("Invalid query syntax. Missing ')' for aggregation function.");

        String attribute = remainingQueryString.substring(1, closingBracketIdx).strip();
        if (attribute.isEmpty())
            throw new QueryParsingException("Invalid query syntax. Missing for aggregation attribute.");
        else if (attribute.contains("("))
            throw new QueryParsingException("Unsupported query. No nesting in aggregation allowed.");

        query.setAttribute(attribute);
        return remainingQueryString.substring(closingBracketIdx + 1);
    }

    /**
     * Sets the table name of the passed {@link ParsedQuery} object from a remaining query like " from table ...;".
     *
     * @param remainingQueryString a remaining query string like " from table ...;".
     * @param query                a {@link ParsedQuery} object representing the full query string.
     * @return the substring of the remaining query string after the table name.
     */
    private static String setQueryTable(String remainingQueryString, ParsedQuery query) throws QueryParsingException {
        String errorMessageLeadingWS = "Invalid query syntax. No whitespace after aggregation.";
        remainingQueryString = checkLeadingWhitespaceAndStrip(remainingQueryString, errorMessageLeadingWS);

        String from = "from";
        if (!remainingQueryString.toLowerCase().startsWith(from))
            throw new QueryParsingException("Invalid query syntax. Missing keyword 'from'.");
        remainingQueryString = remainingQueryString.substring(from.length());

        errorMessageLeadingWS = "Invalid query syntax. No whitespace after 'from'.";
        remainingQueryString = checkLeadingWhitespaceAndStrip(remainingQueryString, errorMessageLeadingWS);

        // remaining query now like "table ...;"

        // get the index of the next whitespace and semicolon; queries like "select count(*) from table;" as well as
        // "select count(*) from table where x=y;"are supported, therefore we have to get the next index of both
        int nextWhitespaceIndex = getNextWhitespaceIndex(remainingQueryString);
        int nextSemicolonIndex = remainingQueryString.indexOf(";", 1);

        if (nextWhitespaceIndex != -1) {
            query.setTable(remainingQueryString.substring(0, nextWhitespaceIndex));
            return remainingQueryString.substring(nextWhitespaceIndex);
        } else { // nextSemicolonIndex != -1 has to hold because the query has to end with a semicolon [checked in parseQuery(...)]
            query.setTable(remainingQueryString.substring(0, nextSemicolonIndex));
            return remainingQueryString.substring(nextSemicolonIndex);
        }
    }

    /**
     * Sets the conditions in the {@link Map} in the passed {@link ParsedQuery} object from remaining queries like " ;", " x=y;",
     * " a=b and c=d and ... and x=y;" or " a=b or c=d or ... or x=y;".
     *
     * @param remainingQueryString like " ;", x=y;", "a=b and c=d and ... and x=y;" or "a=b or c=d or ... or x=y;".
     * @param query                a {@link ParsedQuery} object representing the full query string.
     * @throws QueryParsingException if the query syntax is invalid or different boolean operations were detected.
     */
    private static void setQueryConditions(String remainingQueryString, ParsedQuery query) throws QueryParsingException {
        if (remainingQueryString.stripLeading().startsWith(";")) return;

        String errorMessageLeadingWS = "Invalid query syntax. No whitespace after table name.";
        remainingQueryString = checkLeadingWhitespaceAndStrip(remainingQueryString, errorMessageLeadingWS);

        String where = "where";
        if (!remainingQueryString.toLowerCase().startsWith(where))
            throw new QueryParsingException("Invalid query syntax. Missing keyword 'where'.");
        remainingQueryString = remainingQueryString.substring(where.length());

        if (remainingQueryString.contains("<") || remainingQueryString.contains(">"))
            throw new QueryParsingException("Unsupported query. Only supporting '='-conditions.");

        parseConditions(remainingQueryString, query, null);
    }

    private static void parseConditions(String remainingQueryString, ParsedQuery query, ConditionalType conditionalType) throws QueryParsingException {
        String errorMessageLeadingWS = "Invalid query syntax. Missing whitespace in conditions.";
        remainingQueryString = checkLeadingWhitespaceAndStrip(remainingQueryString, errorMessageLeadingWS);

        // only conditions with "=" allowed
        int eqIdx = remainingQueryString.indexOf("=");
        if (eqIdx == -1) throw new QueryParsingException("Unsupported query. Only supporting '='-conditions.");
        String attribute = remainingQueryString.substring(0, eqIdx).strip();

        remainingQueryString = remainingQueryString.substring(eqIdx + 1).stripLeading();

        // get the index after the last character that the attribute determined above should to equal to (supporting integers and strings)
        int valEndIdx = getValueEndIndex(remainingQueryString);
        String valueString = remainingQueryString.substring(0, valEndIdx);

        // set the query condition in the Query object
        query.getConditions().add(new QueryCondition(attribute, valueString));

        remainingQueryString = remainingQueryString.substring(valEndIdx);

        if (remainingQueryString.stripLeading().startsWith(";")) {
            if (remainingQueryString.stripLeading().length() > 1)
                throw new QueryParsingException("Invalid query syntax. Characters detected after ';'.");
            else return;
        }

        errorMessageLeadingWS = "Invalid query syntax. Missing whitespace in conditions.";
        remainingQueryString = checkLeadingWhitespaceAndStrip(remainingQueryString, errorMessageLeadingWS);

        // check for all possible boolean operators
        for (ConditionalType t : ConditionalType.values()) {
            if (remainingQueryString.toLowerCase().startsWith(t.token)) {
                if (conditionalType != null && t != conditionalType) {
                    throw new QueryParsingException("Invalid query syntax. Multiple boolean operators found. "
                                                    + "Only supporting conjunctive or disjunctive queries.");
                } else {
                    if (conditionalType == null) query.setConditionalType(t);
                    parseConditions(remainingQueryString.substring(t.token.length()), query, t);
                    return;
                }
            }
        }

        throw new QueryParsingException("Invalid query syntax. No matching boolean operator found in the conditional part.");
    }

    private static int getValueEndIndex(String remainingQueryString) throws QueryParsingException {
        int valEndIdx;
        String errorString = null;
        if (remainingQueryString.startsWith("'")) { // condition like "x='y'"
            valEndIdx = remainingQueryString.indexOf("'", 1) + 1; // include the ' in the value string
            errorString = "Invalid query syntax. Missing a closing ' for a string value.";
        } else { // condition like "x=0"
            valEndIdx = getNextWhitespaceIndex(remainingQueryString);
            if (valEndIdx == -1) {
                errorString = "Invalid query syntax. Missing whitespace after a condition value.";
                valEndIdx = remainingQueryString.indexOf(";"); // has to exist, checked previously
            }
        }
        if (valEndIdx == -1) throw new QueryParsingException(errorString);
        return valEndIdx;
    }

    private static int getNextWhitespaceIndex(String remainingQueryString) {
        for (int i = 0; i < remainingQueryString.length(); i++) {
            if (Character.isWhitespace(remainingQueryString.charAt(i))) return i;
        }
        return -1;
    }

    private static String checkLeadingWhitespaceAndStrip(String subQuery, String errorMessage) throws QueryParsingException {
        if (subQuery.isEmpty() || !Character.isWhitespace(subQuery.charAt(0))) {
            throw new QueryParsingException(errorMessage);
        }
        return subQuery.stripLeading();
    }

}
