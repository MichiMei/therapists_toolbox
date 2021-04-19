package Client.Library.Controller.Games;

import Client.Library.Controller.ClientController;
import Client.Library.GUI.ClientGui;
import Library.BadGameIDException;
import Library.ContentPanes.Games.FastReadPane;
import Library.ContentPanes.Games.GRCFastRead;
import Library.ContentPanes.Games.GTCFastRead;
import Library.Protocol.MCGameTransmit;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;

public class FastReadController implements GameControllerCreator.GameController {
    public static final int ID = 0;

    private final FastReadPane gui;
    private final ClientController controller;

    /**
     * Create FastRead GameController
     * @param tmp reference to MainGUI to create game-specific GUI
     * @throws BadGameIDException thrown if game-specific GUI does not exist
     */
    public FastReadController(ClientGui tmp, ClientController controller) throws BadGameIDException {
        gui = (FastReadPane) tmp.startedGame(ID, this);
        this.controller = controller;
    }

    public void displayFin() {
        controller.sendGameReply(new GRCFastRead());
    }

    @Override
    public int getID() {
        return ID;
    }

    @Override
    public void messageReceived(MessageContent transmit) throws ProtocolViolationException {
        if (transmit.getType() != MCGameTransmit.TYPE_ID) throw new ProtocolViolationException("FastRead received wrong message type");
        MCGameTransmit.GTContent tmp = ((MCGameTransmit)transmit).getContent();
        if (tmp.getGameID() != ID) throw new ProtocolViolationException("FastRead received message for different game");
        GTCFastRead content = (GTCFastRead) tmp;
        gui.setToken(content.getToken());
        gui.display(content.getMillis());
    }
}
