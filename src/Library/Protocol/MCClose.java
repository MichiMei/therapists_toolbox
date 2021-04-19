package Library.Protocol;

/**
 * Message Type 01 00 Close
 * Bidirectional
 * Close connection -> both close connection
 * requires 4 Byte errorCode
 */
public class MCClose extends MessageContent {
    public static final short TYPE_ID = 0x0100;

    /**
     * 1: closed by user
     * 2: protocol violation
     * 3: password wrong
     */
    private final int errorCode;

    public MCClose(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "Close-0100:[errorCode:" + errorCode + "]";
    }
}
