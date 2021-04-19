package Client.Library.Controller;

import Client.Library.Controller.Games.GameControllerCreator;
import Client.Library.GUI.ClientGui;
import Client.Library.ConnectionLayer.ClientConnector;
import Library.BadGameIDException;
import Library.ConnectionLayer.Address;
import Library.Protocol.*;
import Library.UnimplementedException;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main Class of Client Program
 * Controls all processes and starts different sub-programs
 */
public class ClientController {

    private static final int VERSION = 0x00010001;

    private final Logger logger;
    private final ClientGui gui;
    private ClientConnector.Connection connection = null;   // null while no connection active
    private GameControllerCreator.GameController gameController = null;
    private ClientConnector.Receiver receiver = null;

    private State state = State.Offline;

/*--------------------------------------------------CONNECTION_LAYER--------------------------------------------------*/

    public synchronized void messageReceived(Message msg) {
        int type = msg.getType();
        int family = type >> 8;
        switch (family) {
            case 1:
                if (type != MCClose.TYPE_ID) {
                    // protocol violation
                    logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                    connection.close(2);
                } else {
                    // closed
                    gui.disconnected();
                    state = State.Offline;
                    connection = null;
                    gameController = null;
                    receiver = null;
                    logger.severe("Host closed connection\n" + msg.toString());
                }
                state = State.Offline;
                connection = null;
                break;
            case 2:
                if (type == 0x0201 && state == State.Connected) {   // TODO get type statically
                    // TODO start work-sheet presentation
                    throw new UnimplementedException("ClientController::messageReceived(...) unimpl");
                } else if (state == State.WorkSheet) {
                    // TODO forward message
                    throw new UnimplementedException("ClientController::messageReceived(...) unimpl");
                } else {
                    // protocol violation
                    logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                    connection.close(2);
                    state = State.Offline;
                    connection = null;
                }
                break;
            case 3:
                if (type == MCGameStart.TYPE_ID && state == State.Connected) {
                    // start game
                    MCGameStart content = (MCGameStart) msg.getContent();
                    try {
                        gameController = GameControllerCreator.create(content.getGameID(), gui, this);
                        logger.info("started game");
                        state = State.Game;
                    } catch (BadGameIDException e) {
                        logger.severe("game id does not exist\n" + e.toString() + "\nshutting down");
                        System.exit(1);
                    }
                } else if (type == MCGameEnd.TYPE_ID && state == State.Game) {
                    // end game
                    gui.ended();
                    gameController = null;
                    state = State.Connected;
                } else if (state == State.Game) {
                    // forward content
                    try {
                        gameController.messageReceived(msg.getContent());
                    } catch (ProtocolViolationException e) {
                        // protocol violation
                        logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                        connection.close(2);
                        state = State.Offline;
                        connection = null;
                    }
                } else {
                    // protocol violation
                    logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                    connection.close(2);
                    state = State.Offline;
                    connection = null;
                }
                break;
            default:
                // protocol violation
                logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                connection.close(2);
                state = State.Offline;
                connection = null;
        }
    }

/*--------------------------------------------------------GUI--------------------------------------------------------*/

    /**
     * Connect pressed by User
     * Try to establish connection to host
     * [blocking]
     * @param address address of the host
     * @param password password of host
     * @return 0:connected; -1:failed; -2:password-wrong
     */
    public int connect(Address address, String password) {
        try {
            connection = new ClientConnector(address, password, VERSION).connect();
        } catch (IOException e) {
            logger.severe("connecting failed\n" + e.toString() + "\nshutting down");
            e.printStackTrace();
            System.exit(1);
        } catch (ClientConnector.PasswordWrongException e) {
            logger.warning("connecting failed, password wrong");
            return -2;
        }

        if (connection != null) {
            state = State.Connected;
            gui.connected();
            receiver = new ClientConnector.Receiver(this, connection);
            new Thread(receiver).start();
            return 0;
        } else {
            return -1;
        }
    }

/*---------------------------------------------------SUB_CONTROLLER---------------------------------------------------*/

    public void sendGameReply(MCGameReply.GRContent content) {
        MCGameReply tmp = new MCGameReply(content);
        connection.sendMessage(new Message(tmp.getType(), tmp));
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Start Client program
     * @param args nothing parsed
     */
    public static void main(String[] args) {
        new ClientController();
    }

    /**
     * Create Client program
     */
    public ClientController() {
        logger = Logger.getLogger(ClientController.class.getName());

        // create GUI
        gui = new ClientGui(this);

    }

    private enum State {Offline, Connected, WorkSheet, Game}
}
