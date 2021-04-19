package Client.Library.Controller.Games;

import Client.Library.Controller.ClientController;
import Client.Library.GUI.ClientGui;
import Library.BadGameIDException;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;

/**
 * Generator for GameControllers
 */
public class GameControllerCreator {
    /**
     * Create game specific Controller
     * @param id game ID
     * @param mainGui reference to main GU for creation of game specific GUI
     * @return specific GameController object
     * @throws BadGameIDException thrown if game ID does not exist
     */
    public static GameController create(int id, ClientGui mainGui, ClientController controller) throws BadGameIDException {
        return switch (id) {
            case 0 -> new FastReadController(mainGui, controller);
            default -> throw new BadGameIDException(id);
        };
    }

    /**
     * GameController interface
     */
    public interface GameController {
        /**
         * Get the game-specific ID
         * @return game ID
         */
        int getID();

        /**
         * Forward received Message Object
         * @param content content of the message
         */
        void messageReceived(MessageContent content) throws ProtocolViolationException;
    }
}
