package Protocol;

/**
 * Message Type 01 04 Registration-Accept
 * Host->Client
 * accept registration
 */
public class MCRegistrationAccept extends MessageContent {
    public static final short TYPE_ID = 0x0104;

    public MCRegistrationAccept() {
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    @Override
    public String toString() {
        return "RegistrationAccept-0104:[]";
    }
}
