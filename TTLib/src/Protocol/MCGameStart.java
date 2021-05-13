package Protocol;

/**
 * Message Type 03 01 Game Start
 * Host -> Client
 */
public class MCGameStart extends MessageContent {
    public static final short TYPE_ID = 0x0301;

    private final int gameID;

    public MCGameStart(int gameID) {
        this.gameID = gameID;
    }

    @Override
    public short getType() {
        return TYPE_ID;
    }

    public int getGameID() {
        return gameID;
    }

    @Override
    public String toString() {
        return "GameStart-0301:[gameID:" + gameID + "]";
    }
}