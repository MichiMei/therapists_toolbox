package Library.Protocol;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Message Type 01 02 Hello-Reply
 * Host->Client
 * accept or reject communication
 * requires 1 Byte flags
 */
public class MCHelloReply extends MessageContent {
    public static final short TYPE_ID = 0x0102;

    // success
    /**
     * flags[0]: 1:accepted; 0:rejected
     * flags[1]: 1:pw-required
     */
    private byte flags;

    /**
     * create reply
     * @param accept indicates weather the connection attempt is accepted or not
     * @param pwRequired indicates weather a password is required or not
     */
    public MCHelloReply(boolean accept, boolean pwRequired) {
        this.flags = 0;
        if (accept)     flags += 0b10000000;
        if (pwRequired) flags += 0b01000000;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public boolean isAccepted() {
        return (flags & 0b10000000) != 0;
    }

    public boolean isPwRequired() {
        return (flags & 0b01000000) != 0;
    }
}

