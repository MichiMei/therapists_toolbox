package Host.Controller;

import Exceptions.UnimplementedException;
import Host.Controller.Games.GameControllerCreator;
import Host.CustomLogger;
import Host.GUI.HostGui;
import Host.ConnectionLayer.HostConnector;
import Host.GUI.Dialogs;
import Host.GUI.SettingsDialog;
import Exceptions.BadGameIDException;
import ConnectionLayer.Address;
import ConnectionLayer.ConnectionTools;
import Protocol.*;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

/**
 * Main Class of Host Program
 * Controls all processes and starts different sub-programs
 */
public class HostController {
    private final CustomLogger logger;
    private final PreferenceStorage prefs;
    private Storage storage;
    private final HostGui gui;
    private final HostConnector connector;
    private HostConnector.Connection connection;
    private GameControllerCreator.GameController gameController;
    private State state = State.Offline;
    private HostConnector.Receiver receiver;

/*--------------------------------------------------ConnectionLayer--------------------------------------------------*/

    /**
     * Connection status changed
     * Notify GUI
     * @param status new connection status
     * @param msg special message to display
     */
    public void connectionStatus(HostConnector.Status status, HostConnector.Connection connection, String msg) {
        if (status == HostConnector.Status.Connected) {
            this.state = State.Connected;
            this.connection = connection;
            this.receiver = new HostConnector.Receiver(this, connection);
            new Thread(receiver).start();
        }
        gui.connectionStatusChanged(status, msg);
    }

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
                    connector.disconnect(2);
                } else {
                    // closed
                    endGame();
                    logger.severe("Host closed connection\n" + msg.toString());
                    connector.disconnect(0);
                }
                state = State.Offline;
                connection = null;
                break;
            case 2:
                if (type == 0x0203 && state == State.WorkSheet) {   // TODO get type statically
                    // TODO forward message
                    throw new UnimplementedException("ClientController::messageReceived(...) unimpl");
                } else {
                    // protocol violation
                    logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                    connector.disconnect(2);
                    state = State.Offline;
                    connection = null;
                }
                break;
            case 3:
                if (type == MCGameReply.TYPE_ID && state == State.Game) {
                    // forward content
                    try {
                        gameController.messageReceived(msg.getContent());
                    } catch (ProtocolViolationException e) {
                        // protocol violation
                        logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                        connector.disconnect(2);
                        state = State.Offline;
                        connection = null;
                    }
                } else {
                    // protocol violation
                    logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                    connector.disconnect(2);
                    state = State.Offline;
                    connection = null;
                }
                break;
            default:
                // protocol violation
                logger.severe("Host protocol violation\n" + msg.toString() + "\nclosing connection");
                connector.disconnect(2);
                state = State.Offline;
                connection = null;
        }
    }

/*--------------------------------------------------------GUI--------------------------------------------------------*/

    /**
     * Get and return info about a user selected file or folder
     * @param path path to the selected element
     * @return File/Folder info
     * @throws Storage.FaultyStorageStructureException thrown if path not existing
     */
    public Storage.Info getFileInfo(TreePath path) throws Storage.FaultyStorageStructureException {
        return storage.getInfo(path);
    }

    /**
     * Get and return info about a user selected game
     * @param gameID id of the selected game
     * @return game info
     */
    public GameControllerCreator.Info getGameInfo(int gameID) throws BadGameIDException {
        return GameControllerCreator.getInfo(gameID);
    }

    /**
     * Try to go online (start listening for incoming connections)
     * @param password password for authentication demanded by client
     * @return true if successful, false otherwise (e.g. no internet connection)
     */
    public boolean goOnline(String password) {
        logger.info("start listening");
        try {
            connector.startListening(prefs.getPort(), password);
            return true;
        } catch (IOException e) {
            logger.warning("Could not create socket\n" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Go offline (stop listening for incoming connections)
     */
    public void goOffline() {
        logger.info("stop listening");
        connector.stopListening();
    }

    /**
     * Disconnect from client
     */
    public void disconnect() {
        logger.info("disconnect");
        connector.disconnect(1);
    }

    /**
     *
     */
    public void exit() {
        logger.info("exiting");
        if (state != State.Offline) {
            disconnect();
        }
        logger.closeHandlers();
        System.exit(0);
    }

    /**
     * Get own address to display in GUI
     * @return own address
     */
    public Address getAddress() {
        return getOwnAddress();
    }

    /**
     * User started work sheet presentation
     * @param path selected work sheet
     */
    public void startWorkSheet(TreePath path) {
        // TODO
        throw new UnimplementedException("HostController::startWorksheet(...) unimpl");
    }

    /**
     * User started game
     * @param id id of selected game
     */
    public void startGame(int id) {
        try {
            gameController = GameControllerCreator.create(id, gui, this);
            MessageContent content = new MCGameStart(id);
            connection.sendMessage(new Message(content.getType(), content));
            state = State.Game;
            logger.info("game started");
        } catch (BadGameIDException e) {
            logger.severe("game id does not exist\n" + e.getMessage() + "\nshutting down");
            e.printStackTrace();
            System.exit(-1);
        }
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
     * Forward game transmit
     * game-controller -> main-controller -> connection-layer
     * @param content content of the transmit message
     */
    public void sendGameTransmit(MCGameTransmit.GTContent content) {
        MCGameTransmit tmp = new MCGameTransmit(content);
        Message msg = new Message(tmp.getType(), tmp);
        connection.sendMessage(msg);
    }

    /**
     * User ended game
     * Notify main gui
     */
    public void endGame() {
        MessageContent content = new MCGameEnd();
        connection.sendMessage(new Message(content.getType(), content));
        gui.endedGame();
        gameController = null;
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Start Host program
     * @param args nothing parsed
     */
    public static void main(String[] args) throws IOException {
        new HostController();
    }

    /**
     * Create Host program
     */
    public HostController() throws IOException {
        logger = CustomLogger.getInstance();

        // read preferences
        logger.info("Reading Preferences");
        prefs = new PreferenceStorage();

        // set default font size
        setDefaultFontSize(prefs.getDefaultFontSize());

        // create storage
        loadStorage();
        logger.addHandler(storage.getFileLog());

        // create GUI
        gui = new HostGui(this);
        gui.reloadSheetTree(storage.getJTreeRoot());

        // create Connector
        this.connector = new HostConnector(this);
    }

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
     * Loads the storage
     * (if no storage is found, asks the user for the path to the new one to create or a existing one to check)
     */
    private void loadStorage() throws IOException {
        logger.info("loading storage...");


        while (prefs.getStoragePath() == null) {   // first start
            try {
                boolean newLocation = Dialogs.showStartDialog();
                Path tmpPath = Dialogs.chooseLocation(Storage.defaultPath());
                if (newLocation) {
                    logger.info("creating new storage...");
                    Path newPath = Storage.initStorage(tmpPath);
                    if (newPath == null) {
                        // base-dir already existed
                        logger.warning("location already in use");
                        // TODO inform user, ask for new location or cancel
                        throw new UnimplementedException("src/Host/Main.java: loadStorage(...) is unimplemented");
                    }
                    prefs.setStoragePath(newPath.toString());
                    logger.info("new storage created");
                } else {
                    logger.info("checking existing storage...");
                    Path newPath = Storage.checkStorageSystem(tmpPath);
                    prefs.setStoragePath(newPath.toString());
                    logger.info("existing storage intact");
                }
            } catch (Dialogs.CancelPressedException e) {
                logger.severe("User canceled storage creation, exiting!");
                logger.severe(e.getLocalizedMessage());
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                logger.severe("IOException occurred, exiting!");
                logger.severe(e.getLocalizedMessage());
                e.printStackTrace();
                System.exit(1);
            } catch (Storage.FaultyStorageStructureException | Storage.CorruptedStorageException e) {
                logger.warning("existing storage is damaged");
                logger.severe(e.getLocalizedMessage());
                e.printStackTrace();
                // TODO inform user, ask for new location or cancel
                throw new UnimplementedException("src/Host/Main.java: loadStorage(...) is unimplemented");
            }
        }

        Path storagePath = Paths.get(prefs.getStoragePath());

        try {
            storage = new Storage(storagePath);
        } catch (Storage.FaultyStorageStructureException | Storage.CorruptedStorageException e) {
            logger.severe("Storage corrupted, exiting!");
            logger.severe(e.getLocalizedMessage());
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("storage loaded");
    }

    /**
     * Get own address
     * @return own address
     */
    private Address getOwnAddress() {
        try {
            return new Address(ConnectionTools.getExternalIP(), prefs.getPort());
//            return new Address(ConnectionTools.getLocalIP(), HostConnector.DEFAULT_PORT);
        } catch (IOException e) {
            logger.warning("getIP failed\n" + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Address.BadIPAddressException e) {
            logger.severe("Address creation failed\n" + e.getMessage() + "\nshutting down");
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }

    /**
     * Preference Storage manager
     */
    static class PreferenceStorage {
        private final Preferences prefs;

        private String storagePath;
        private int port;
        private int defaultFontSize;

        /**
         * Create preference storage manager
         */
        public PreferenceStorage() {
            prefs = Preferences.userNodeForPackage(getClass());
            storagePath = loadString("storagePath");
            port = loadInt("port");
            if (port == -1) setPort(HostConnector.DEFAULT_PORT);
            defaultFontSize = loadInt("defaultFontSize");
            if (defaultFontSize < 50 || defaultFontSize > 300) setDefaultFontSize(100);
        }

        /**
         * Get path to sheet storage
         * @return storage path
         */
        public String getStoragePath() {
            return storagePath;
        }

        /**
         * Set path to sheet storage
         * @param storagePath new path
         */
        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
            store("storagePath", storagePath);
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

    private enum State {Offline, Connected, Game, WorkSheet}
}
