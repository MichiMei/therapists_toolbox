package Library.Protocol;

import java.io.Serializable;

public class Message implements Serializable {
    protected short type;
    protected MessageContent content;

    public Message(short type, MessageContent content) {
        this.type = type;
        this.content = content;
    }

    public short getType() {
        return type;
    }

    public MessageContent getContent() {
        return content;
    }
}
