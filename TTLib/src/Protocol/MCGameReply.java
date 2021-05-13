package Protocol;

import java.io.Serializable;

/**
 * Message Type 03 03 Game Reply
 * Client -> Host
 */
public class MCGameReply extends MessageContent {
    public static final short TYPE_ID = 0x0303;

    private GRContent content;

    public MCGameReply(GRContent content) {
        this.content = content;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public GRContent getContent() {
        return content;
    }

    public abstract static class GRContent implements Serializable {
        public abstract int getGameID();
    }

    @Override
    public String toString() {
        return "GameReply-0303:[content:" + content.toString() + "]";
    }
}