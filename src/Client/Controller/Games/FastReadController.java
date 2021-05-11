package Client.Controller.Games;

import Client.Controller.ClientController;
import Client.GUI.ClientGui;
import Library.BadGameIDException;
import Library.ContentPanes.Games.FastReadPane;
import Library.ContentPanes.Games.GRCFastRead;
import Library.ContentPanes.Games.GTCFastRead;
import Library.Protocol.MCGameTransmit;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;

import java.util.logging.Logger;

/**
 * Specialized Game Controller for Fast-Read
 */
public class FastReadController implements GameControllerCreator.GameController {
    public static final int ID = 0;

    private final FastReadPane gui;
    private final ClientController controller;
    private final Logger logger;

/*--------------------------------------------------MAIN_CONTROLLER--------------------------------------------------*/

    /**
     * Create FastRead GameController
     * @param tmp reference to MainGUI to create game-specific GUI
     * @throws BadGameIDException thrown if game-specific GUI does not exist
     */
    public FastReadController(ClientGui tmp, ClientController controller) throws BadGameIDException {
        gui = (FastReadPane) tmp.startedGame(ID, this);
        this.controller = controller;
        this.logger = Logger.getLogger(ClientController.class.getName());
    }

    /**
     * MessageReceived
     * Parse and Execute
     * @param transmit received massage content
     * @throws ProtocolViolationException thrown if received a protocol violating message
     */
    @Override
    public void messageReceived(MessageContent transmit) throws ProtocolViolationException {
        if (transmit.getType() != MCGameTransmit.TYPE_ID) {
            logger.warning("Protocol violation: Host send non game transmit\n" + transmit.toString());
            throw new ProtocolViolationException("FastRead received wrong message type");
        }
        MCGameTransmit.GTContent tmp = ((MCGameTransmit)transmit).getContent();
        if (tmp.getGameID() != ID) {
            logger.warning("Protocol violation: Host send game transmit for wrong game\n" + transmit.toString());
            throw new ProtocolViolationException("FastRead received message for different game");
        }
        GTCFastRead content = (GTCFastRead) tmp;
        gui.setToken(content.getToken());
        gui.display(content.getMillis());
    }

/*--------------------------------------------------------GUI--------------------------------------------------------*/

    /**
     * Notification by GUI -> displaying finished -> notify host
     */
    public void displayFin() {
        controller.sendGameReply(new GRCFastRead());
    }

/*--------------------------------------------------------ALL--------------------------------------------------------*/

    /**
     * Get gameID (0) of corresponding game
     * @return game id (0)
     */
    @Override
    public int getID() {
        return ID;
    }


}
