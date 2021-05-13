package Host.Controller.Games;

import Exceptions.BadGameIDException;
import Host.GUI.HostGui;
import Host.Controller.HostController;
import Protocol.MessageContent;
import Protocol.ProtocolViolationException;

import java.util.ResourceBundle;

/**
 * Generator for GameControllers
 */
public abstract class GameControllerCreator {

/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create game specific Controller
     * @param id game ID
     * @param mainGui reference to main GU for creation of game specific GUI
     * @return specific GameController object
     * @throws BadGameIDException thrown if game ID does not exist
     */
    public static GameController create(int id, HostGui mainGui, HostController controller) throws BadGameIDException {
        return switch (id) {
            case 0 -> new FastReadController(mainGui, controller);
            default -> throw new BadGameIDException(id);
        };
    }

    /**
     * Return the game info of game id
     * @param id id of the game
     * @return game info
     * @throws BadGameIDException thrown if game id does not exist
     */
    public static Info getInfo(int id) throws BadGameIDException {
        if (id < 0 || id > 0) {
            throw new BadGameIDException(id);
        }
        return new Info(ResourceBundle.getBundle("Resources/StringLiterals").getString("game_name_"+id), ResourceBundle.getBundle("Resources/StringLiterals").getString("game_description_"+id));
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

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

    /**
     * Class representing game info
     */
    public static class Info {
        private final String name;
        private final String description;

        public Info(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }
}
