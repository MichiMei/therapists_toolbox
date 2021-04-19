package Library.ContentPanes.Games;

import Library.Protocol.MCGameTransmit;

public class GTCFastRead extends MCGameTransmit.GTContent {
    public static final int gameID = 0;

    private final String token;
    private final int millis;

    public GTCFastRead(String token, int millis) {
        this.token = token;
        this.millis = millis;
    }

    @Override
    public int getGameID() {
        return gameID;
    }

    public String getToken() {
        return token;
    }

    public int getMillis() {
        return millis;
    }

    @Override
    public String toString() {
        return "FastRead:[token:" + token + "; millis:" + millis + "]";
    }
}
