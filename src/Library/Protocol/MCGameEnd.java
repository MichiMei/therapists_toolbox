package Library.Protocol;

/**
 * Message Type 03 00 Game End
 * Host -> Client
 */
public class MCGameEnd extends MessageContent {
    public static final short TYPE_ID = 0x0300;


    public MCGameEnd() {

    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    @Override
    public String toString() {
        return "GameEnd-0300:[]";
    }
}