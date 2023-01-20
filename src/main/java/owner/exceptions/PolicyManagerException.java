package owner.exceptions;

public class PolicyManagerException extends Exception{

    public PolicyManagerException(String message) {
        super("Couldn't parse policy: " + message);
    }
}
