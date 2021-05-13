package Protocol;

import java.io.Serializable;

public abstract class MessageContent implements Serializable {

    public abstract short getType();

}

