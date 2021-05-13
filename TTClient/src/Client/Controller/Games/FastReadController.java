package Client.Controller.Games;

import Client.Controller.ClientController;
import Client.GUI.ClientGui;
import Exceptions.BadGameIDException;
import ContentPanes.Games.FastReadPane;
import ContentPanes.Games.GRCFastRead;
import ContentPanes.Games.GTCFastRead;
import Protocol.MCGameTransmit;
import Protocol.MessageContent;
import Protocol.ProtocolViolationException;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
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
     * Sends GameReply as soon as displaying finished
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
        try {
            SwingUtilities.invokeAndWait(() -> gui.display(content.getMillis(), new FastReadPane.DisplayFinishedCallback() {
                @Override
                public void displayFinished() {
                    controller.sendGameReply(new GRCFastRead());
                }
            }));
        } catch (InterruptedException | InvocationTargetException e) {
            logger.warning("queueing display(...) failed\n" + e.getMessage());
        }
    }

/*--------------------------------------------------------GUI--------------------------------------------------------*/

    /*
     * Notification by GUI -> displaying finished -> notify host
     *
    public void displayFin() {
        controller.sendGameReply(new GRCFastRead());
    }*/

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
