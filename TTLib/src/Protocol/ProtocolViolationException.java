package Protocol;

/**
 * Thrown if the communication partner violates the protocol
 */
public class ProtocolViolationException extends Exception {

    /**
     * Thrown if the communication partner violates the protocol
     * @param msg Message indicating the problem
     */
    public ProtocolViolationException(String msg) {
        super(msg);
    }

}
