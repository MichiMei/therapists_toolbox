package Host.Controller.Games;

import Host.GUI.Games.FastReadPane;
import Host.GUI.HostGui;
import Host.Controller.HostController;
import Library.BadGameIDException;
import Library.ContentPanes.Games.GTCFastRead;
import Library.Protocol.MCGameReply;
import Library.Protocol.MCGameTransmit;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;

import java.util.logging.Logger;

/**
 * Controller for the FastRead game
 */
public class FastReadController implements GameControllerCreator.GameController {
    public static final int ID = 0;

    private final FastReadPane gui;
    private final HostController mainController;

    private final Logger logger;

/*------------------------------------------------------GAME_GUI------------------------------------------------------*/

    /**
     * User pressed display
     * Send client display order
     * @param token token to display
     * @param millis display duration (in milliseconds)
     */
    public void display(String token, int millis) {
        logger.info("display token(" + token + ") for " + millis + "ms");
        MCGameTransmit.GTContent content = new GTCFastRead(token, millis);
        mainController.sendGameTransmit(content);
    }

    /**
     * User pressed end
     * End game
     */
    public void end() {
        logger.info("ending game");
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
        logger = Logger.getLogger(FastReadController.class.getName());
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
