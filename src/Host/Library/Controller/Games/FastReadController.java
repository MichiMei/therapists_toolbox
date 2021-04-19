package Host.Library.Controller.Games;

import Host.Library.Controller.HostController;
import Host.Library.GUI.Games.FastReadPane;
import Host.Library.GUI.HostGui;
import Library.BadGameIDException;
import Library.ContentPanes.Games.GRCFastRead;
import Library.ContentPanes.Games.GTCFastRead;
import Library.Protocol.MCGameReply;
import Library.Protocol.MCGameTransmit;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;
import Library.UnimplementedException;

/**
 * Controller for the FastRead game
 */
public class FastReadController implements GameControllerCreator.GameController {
    public static final int ID = 0;

    private final FastReadPane gui;
    private final HostController mainController;

/*------------------------------------------------------GAME_GUI------------------------------------------------------*/

    public void display(String token, int millis) {
        MCGameTransmit.GTContent content = new GTCFastRead(token, millis);
        mainController.sendGameTransmit(content);
    }

    public void end() {
        mainController.endGame();
    }

/*--------------------------------------------------MAIN_CONTROLLER--------------------------------------------------*/

    /**
     * Create FastRead GameController
     * @param tmp reference to MainGUI to create game-specific GUI
     * @throws BadGameIDException thrown if game-specific GUI does not exist
     */
    public FastReadController(HostGui tmp, HostController controller) throws BadGameIDException {
        gui = (FastReadPane) tmp.startedGame(ID, this);
        this.mainController = controller;
    }

    /**
     * Forward received Message Object
     * @param reply content of the message
     */
    @Override
    public void messageReceived(MessageContent reply) throws ProtocolViolationException {
        if (reply.getType() != MCGameReply.TYPE_ID) throw new ProtocolViolationException("FastRead received wrong message type");
        MCGameReply.GRContent tmp = ((MCGameReply)reply).getContent();
        if (tmp.getGameID() != ID) throw new ProtocolViolationException("FastRead received message for different game");
        GRCFastRead content = (GRCFastRead) tmp;
        gui.displayFinished();
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Return the game specific ID
     * @return game ID (0)
     */
    @Override
    public int getID() {
        return ID;
    }
}
