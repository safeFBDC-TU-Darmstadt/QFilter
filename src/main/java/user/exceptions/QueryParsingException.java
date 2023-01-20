package user.exceptions;

import java.io.Serial;

public class QueryParsingException extends Exception {

    @Serial
    private static final long serialVersionUID = -4978436319714445467L;

    public QueryParsingException(String message) {
        super("Couldn't parse query: " + message);
    }
}
