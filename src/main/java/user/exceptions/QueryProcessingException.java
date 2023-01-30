package user.exceptions;

import java.io.Serial;

public class QueryProcessingException extends Exception {
    @Serial
    private static final long serialVersionUID = 1389084912722218539L;

    public QueryProcessingException(String message) {
        super("Could not process query: " + message);
    }
}
