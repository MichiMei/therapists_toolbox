package Host.Library.Controller;

import Host.Library.GUI.HostGui;
import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.GUI.Dialogs;
import Library.ConnectionLayer.Address;
import Library.ConnectionLayer.ConnectionTools;
import Library.ContentClasses.UnimplementedException;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * GUI <-> Controller <-> ConnectionLayer
 */
public class HostController {
    private final Logger logger;
    private final PreferenceStorage prefs;
    private Storage storage;
    private final HostGui gui;
    private final HostConnector connector;

    public static void main(String[] args) throws IOException {
        new HostController();
    }

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
/*--------------------------------------------------ConnectionLayer--------------------------------------------------*/

    public void connectionStatus(HostConnector.Status status, String msg) {
        gui.connectionStatusChanged(status, msg);
    }

/*--------------------------------------------------------GUI--------------------------------------------------------*/
    public Storage.Info getFileInfo(TreePath path) throws Storage.FaultyStorageStructureException {
        return storage.getInfo(path);
    }

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

    public void goOffline() {
        logger.info("stop listening");
        connector.stopListening();
    }

    public void disconnect() {
        logger.info("disconnect");
        connector.disconnect();
    }

    public Address getAddress() {
        return getOwnAddress();
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/
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

    private Address getOwnAddress() {
        try {
            // TODO switch to external IP
            return new Address(ConnectionTools.getLocalIP(), HostConnector.DEFAULT_PORT);
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

    static class PreferenceStorage {
        private final Preferences prefs;

        private String storagePath;
        private int port;

        public PreferenceStorage() {
            prefs = Preferences.userNodeForPackage(getClass());
            storagePath = loadString("storagePath");
            port = loadInt("port");
            if (port == -1) setPort(HostConnector.DEFAULT_PORT);
        }

        public String getStoragePath() {
            return storagePath;
        }

        public void setStoragePath(String storagePath) {
            this.storagePath = storagePath;
            store("storagePath", storagePath);
        }

        public void setPort(int port) {
            this.port = port;
            store("port", port);
        }

        private String loadString(String key) {
            return prefs.get(key, null);
        }

        private int loadInt(String key) {
            return prefs.getInt(key, -1);
        }

        private void store(String key, String value) {
            prefs.put(key, value);
        }

        private void store(String key, int value) {
            prefs.putInt(key, value);
        }
    }
}
