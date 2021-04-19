package Host.Library.Controller;

import Host.Library.Controller.Games.GameControllerCreator;
import Host.Library.GUI.HostGui;
import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.GUI.Dialogs;
import Library.BadGameIDException;
import Library.ConnectionLayer.Address;
import Library.ConnectionLayer.ConnectionTools;
import Library.Protocol.*;
import Library.UnimplementedException;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Main Class of Host Program
 * Controls all processes and starts different sub-programs
 */
public class HostController {
    private final Logger logger;
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
                    // TODO reset GUI
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
            connector.startListening(HostConnector.DEFAULT_PORT, password);
            return true;
        } catch (IOException e) {
            logger.warning("Could not create socket\n" + e.toString());
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
     * Get own address to display in GUI
     * @return own address
     */
    public Address getAddress() {
        return getOwnAddress();
    }

    public void startWorkSheet(TreePath path) {
        // TODO
        throw new UnimplementedException("HostController::startWorksheet(...) unimpl");
    }

    public void startGame(int id) {
        try {
            gameController = GameControllerCreator.create(id, gui, this);
            MessageContent content = new MCGameStart(id);
            connection.sendMessage(new Message(content.getType(), content));
            state = State.Game;
            logger.info("game started");
        } catch (BadGameIDException e) {
            e.printStackTrace();
            // TODO
            throw new UnimplementedException("HostController::startWorksheet(...) unimpl");
        }
    }

/*---------------------------------------------------SUB_CONTROLLER---------------------------------------------------*/

    public void sendGameTransmit(MCGameTransmit.GTContent content) {
        MCGameTransmit tmp = new MCGameTransmit(content);
        Message msg = new Message(tmp.getType(), tmp);
        connection.sendMessage(msg);
    }

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
        logger = Logger.getLogger(HostController.class.getName());

        // read preferences
        logger.info("Reading Preferences");
        prefs = new PreferenceStorage();

        // create storage
        loadStorage();

        // create GUI
        gui = new HostGui(this);
        gui.reloadSheetTree(storage.getJTreeRoot());

        // create Connector
        this.connector = new HostConnector(this);
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
     * @return
     */
    private Address getOwnAddress() {
        try {
            return new Address(ConnectionTools.getExternalIP(), HostConnector.DEFAULT_PORT);
            //return new Address(ConnectionTools.getLocalIP(), HostConnector.DEFAULT_PORT);
        } catch (IOException e) {
            logger.warning("getIP failed\n" + e.toString());
            e.printStackTrace();
            return null;
        } catch (Address.BadIPAddressException e) {
            logger.severe("Address creation failed\n" + e.toString() + "\nshutting down");
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

        /**
         * Create preference storage manager
         */
        public PreferenceStorage() {
            prefs = Preferences.userNodeForPackage(getClass());
            storagePath = loadString("storagePath");
            port = loadInt("port");
            if (port == -1) setPort(HostConnector.DEFAULT_PORT);
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
