package Host.Library.Controller.Games;

import Host.Library.Controller.HostController;
import Host.Library.Controller.Storage;
import Host.Library.GUI.HostGui;
import Library.BadGameIDException;
import Library.Protocol.MessageContent;
import Library.Protocol.ProtocolViolationException;

import java.util.ResourceBundle;

/**
 * Generator for GameControllers
 */
public abstract class GameControllerCreator {

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

    public static Info getInfo(int id) throws BadGameIDException {
        if (id < 0 || id > 0) {
            throw new BadGameIDException(id);
        }
        return new Info(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("game_name_"+id), ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("game_description_"+id));
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
