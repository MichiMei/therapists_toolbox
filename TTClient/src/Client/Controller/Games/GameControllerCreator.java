package Client.Controller.Games;

import Client.Controller.ClientController;
import Client.GUI.ClientGui;
import Exceptions.BadGameIDException;
import Protocol.MessageContent;
import Protocol.ProtocolViolationException;

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
