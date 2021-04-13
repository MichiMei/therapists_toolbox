package Library.Protocol;

/**
 * Thrown if the TCP-Stream was closed by the opposing side
 */
public class ConnectionClosedException extends Exception {

    /**
     * Thrown if the TCP-Stream was closed by the opposing side
     * @param msg Message explaining the situation
     */
    public ConnectionClosedException(String msg) {
        super(msg);
    }

}
