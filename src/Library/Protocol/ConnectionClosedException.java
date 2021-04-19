package Library.Protocol;

/**
 * Thrown if the TCP-Stream was closed by the opposing side
 */
public class ConnectionClosedException extends Exception {

    private final int code;

    /**
     * Thrown if the TCP-Stream was closed by the opposing side
     * @param msg Message explaining the situation
     */
    public ConnectionClosedException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public ConnectionClosedException(int code) {
        super("Connection closed! Reason: " + code);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
