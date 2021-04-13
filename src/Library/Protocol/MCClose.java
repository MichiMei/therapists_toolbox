package Library.Protocol;

/**
 * Message Type 01 00 Close
 * Bidirectional
 * Close connection -> both close connection
 * requires 4 Byte errorCode
 */
public class MCClose extends MessageContent {
    public static final short TYPE_ID = 0x0100;

    private int errorCode;

    public MCClose(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }
}
