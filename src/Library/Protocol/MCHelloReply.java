package Library.Protocol;

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
     * flags[0]: 1:pw-required
     */
    private byte flags;

    /**
     * create reply
     * @param pwRequired indicates weather a password is required or not
     */
    public MCHelloReply(boolean pwRequired) {
        this.flags = 0;
        if (pwRequired) flags += 0b10000000;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public boolean isPwRequired() {
        return (flags & 0b10000000) != 0;
    }
}

