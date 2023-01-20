package owner.exceptions;

import java.io.Serial;

public class UploadException extends Exception {

    @Serial
    private static final long serialVersionUID = -8363288493855310605L;

    public UploadException(String message) {
        super(message);
    }

}
