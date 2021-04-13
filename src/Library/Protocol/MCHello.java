package Library.Protocol;

import java.math.BigInteger;

/**
 * Message Type 01 01 Hello
 * Client->Host
 * initiate communication
 * requires 4 Byte Version
 */
public class MCHello extends MessageContent {
    public static final short TYPE_ID = 0x0101;

    private int version;

    public MCHello (int  version) {
        this.version = version;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public int getVersion() {
        return version;
    }
}

