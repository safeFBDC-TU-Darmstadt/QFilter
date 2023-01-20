package owner.exceptions;

public class CredentialManagerExceptions extends Exception{

    public CredentialManagerExceptions(String message) {
        super("Couldn't : " + message);
    }
}
