package owner.exceptions;

public class ExpressionParsingException extends Exception{
    public ExpressionParsingException(String message) {
        super("Couldn't parse boolean expression: " + message);
    }
}
