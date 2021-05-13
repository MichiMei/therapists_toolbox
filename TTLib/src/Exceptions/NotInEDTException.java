package Exceptions;

public class NotInEDTException extends NullPointerException {

    public NotInEDTException(String message) {
        super(message);
    }

    public NotInEDTException() {
        super();
    }
}
