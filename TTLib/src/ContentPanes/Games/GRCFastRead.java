package ContentPanes.Games;

import Protocol.MCGameReply;

public class GRCFastRead extends MCGameReply.GRContent {
    public static final int gameID = 0;

    public GRCFastRead() {

    }

    @Override
    public int getGameID() {
        return gameID;
    }

    @Override
    public String toString() {
        return "FastRead[]";
    }
}
