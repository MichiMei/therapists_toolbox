package Client.Controller;

import Client.GUI.ClientGui;
import Client.Controller.Games.GameControllerCreator;
import Client.ConnectionLayer.ClientConnector;
import Client.GUI.SettingsDialog;
import Library.BadGameIDException;
import Library.ConnectionLayer.Address;
import Library.Protocol.*;
import Library.UnimplementedException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Main Class of Client Program
 * Controls all processes and starts different sub-programs
 */
public class ClientController {

    private static final int VERSION = 0x00010001;
    private final PreferenceStorage prefs;

    private final Logger logger;
    private final ClientGui gui;
    private ClientConnector.Connection connection = null;   // null while no connection active
    private GameControllerCreator.GameController gameController = null;
    private ClientConnector.Receiver receiver = null;

    private State state = State.Offline;

/*--------------------------------------------------CONNECTION_LAYER--------------------------------------------------*/

    /**
     * Receiver received message
     * Parse and forward
     * @param msg received message
     */
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
                        logger.severe("game id does not exist\n" + e.getMessage() + "\nshutting down");
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
     * @return 0:connected; -1:address; -2:password-wrong; -3:connection-closed; -4:host-protocol-violation
     */
    public int connect(String address, String password) {
        try {
            connection = new ClientConnector(new Address(address, prefs.getPort()), password, VERSION).connect();
        } catch (IOException e) {
            logger.severe("connecting failed\n" + e.getMessage() + "\nshutting down");
            e.printStackTrace();
            System.exit(-1);
        } catch (ClientConnector.PasswordWrongException e) {
            logger.warning("connecting failed, password wrong");
            e.printStackTrace();
            return -2;
        } catch (ConnectionClosedException e) {
            logger.warning("connecting failed, host closed connection");
            e.printStackTrace();
            return -3;
        } catch (ProtocolViolationException e) {
            logger.warning("connecting failed, host protocol violation\n" + e.getMessage());
            e.printStackTrace();
            return -4;
        } catch (Address.BadIPAddressException e) {
            logger.severe("creating address failed\n" + e.getMessage() + "\nshutting down");
            e.printStackTrace();
            System.exit(-1);
        }

        if (connection != null) {
            state = State.Connected;
            gui.connected();
            receiver = new ClientConnector.Receiver(this, connection);
            new Thread(receiver).start();
            logger.info("receiver started");
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Get current default address
     * @return port
     */
    public String getAddress() {
        return prefs.getAddress();
    }

    /**
     * Set current default address
     * @param address new default address
     */
    public void setAddress(String address) {
        prefs.setAddress(address);
    }

    /**
     * Get current port
     * @return port
     */
    public int getPort() {
        return prefs.getPort();
    }

    /**
     * Set current port
     * @param port new port
     */
    public void setPort(int port) {
        prefs.setPort(port);
    }

    /**
     * Get default font size
     * @return default font size
     */
    public int getDefFontSize() {
        return prefs.getDefaultFontSize();
    }

    /**
     * Set default font size
     * @param defFontSize new default font size
     */
    public void setDefFontSize(int defFontSize) {
        prefs.setDefaultFontSize(defFontSize);
        setDefaultFontSize(defFontSize);
    }

    /**
     * Open settings dialog
     * @param dim size for the dialog
     */
    public void displaySettings(Dimension dim) {
        new SettingsDialog(this, dim);
    }

/*---------------------------------------------------SUB_CONTROLLER---------------------------------------------------*/

    /**
     * Forward game reply
     * Send to cost
     * @param content message content to send
     */
    public void sendGameReply(MCGameReply.GRContent content) {
        MCGameReply tmp = new MCGameReply(content);
        connection.sendMessage(new Message(tmp.getType(), tmp));
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    private void setDefaultFontSize(int scaling) {
        int size = 12*scaling/100;
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements())
        {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
            {
                FontUIResource oldFont = (FontUIResource) value;
                FontUIResource newFont = new FontUIResource(oldFont.getName(), Font.PLAIN, size);
                UIManager.put(key, newFont);
            }
        }

    }

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
        prefs = new PreferenceStorage();

        // set default font size
        setDefaultFontSize(prefs.getDefaultFontSize());

        // create GUI
        gui = new ClientGui(this);
    }

    /**
     * Preference Storage manager
     */
    static class PreferenceStorage {
        private final Preferences prefs;

        private int port;
        private String address;
        private int defaultFontSize;

        /**
         * Create preference storage manager
         */
        public PreferenceStorage() {
            prefs = Preferences.userNodeForPackage(getClass());
            port = loadInt("port");
            if (port == -1) setPort(ClientConnector.DEFAULT_PORT);
            address = loadString("address");
            if (address == null) setAddress("noip.bodo-m.de");
            defaultFontSize = loadInt("defaultFontSize");
            if (defaultFontSize < 50 || defaultFontSize > 300) setDefaultFontSize(100);
        }

        /**
         * Get path to sheet storage
         * @return storage path
         */
        public String getAddress() {
            return address;
        }

        /**
         * Set path to sheet storage
         * @param storagePath new path
         */
        public void setAddress(String storagePath) {
            this.address = storagePath;
            store("address", storagePath);
        }

        /**
         * Get listening port
         * @return port
         */
        public int getPort() {
            return port;
        }

        /**
         * Set listening port
         * @param port new port
         */
        public void setPort(int port) {
            this.port = port;
            store("port", port);
        }

        /**
         * Get default font size
         * @return default font size
         */
        public int getDefaultFontSize() {
            return defaultFontSize;
        }

        /**
         * Set default font size
         * @param defaultFontSize default font size
         */
        public void setDefaultFontSize(int defaultFontSize) {
            this.defaultFontSize = defaultFontSize;
            store("defaultFontSize", defaultFontSize);
        }

        /**
         * Load String preference
         * @param key key of the preference
         * @return value of the preference
         */
        private String loadString(String key) {
            return prefs.get(key, null);
        }

        /**
         * Load int preference
         * @param key key of the preference
         * @return value of the preference
         */
        private int loadInt(String key) {
            return prefs.getInt(key, -1);
        }

        /**
         * Store String preference
         * @param key key of the preference
         * @param value new value of the preference
         */
        private void store(String key, String value) {
            prefs.put(key, value);
        }

        /**
         * Store int preference
         * @param key key of the preference
         * @param value new value of the preference
         */
        private void store(String key, int value) {
            prefs.putInt(key, value);
        }
    }

    private enum State {Offline, Connected, WorkSheet, Game}
}
