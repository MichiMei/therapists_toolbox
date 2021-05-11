package Library.Protocol;

/**
 * Thrown if the TCP-Stream was closed by the opposing side
 */
public class ConnectionClosedException extends Exception {

    private final int code;

    /**
     * Thrown if the TCP-Stream was closed by the opposing side
     * @param msg Message explaining the situation
     * @param code error code
     */
    public ConnectionClosedException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    /**
     * Thrown if the TCP-Stream was closed by the opposing side
     * @param code error code
     */
    public ConnectionClosedException(int code) {
        super("Connection closed! Reason: " + code);
        this.code = code;
    }

    /**
     * Return close error code
     * @return error code
     */
    public int getCode() {
        return code;
    }
}
