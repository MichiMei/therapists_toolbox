package Library.ContentPanes.Games;

import Library.Protocol.MCGameReply;

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
