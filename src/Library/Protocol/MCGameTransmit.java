package Library.Protocol;

import java.io.Serializable;

/**
 * Message Type 03 02 Game Transmit
 * Host -> Client
 */
public class MCGameTransmit extends MessageContent {
    public static final short TYPE_ID = 0x0302;

    private GTContent content;

    public MCGameTransmit(GTContent content) {
        this.content = content;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public GTContent getContent() {
        return content;
    }

    public abstract static class GTContent implements Serializable {
        public abstract int getGameID();
    }

    @Override
    public String toString() {
        return "GameTransmit-0302:[content:" + content.toString() + "]";
    }
}