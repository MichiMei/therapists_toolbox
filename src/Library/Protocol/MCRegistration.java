package Library.Protocol;

import java.math.BigInteger;
import java.util.Optional;

/**
 * Message Type 01 03 Registration
 * Client->Host
 * accept or reject communication
 * requires pw-length, password
 */
public class MCRegistration extends MessageContent {
    public static final short TYPE_ID = 0x0103;

    private String password;

    public MCRegistration(String password) {
        this.password = password;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public String getPassword() {
        return password;
    }
}

