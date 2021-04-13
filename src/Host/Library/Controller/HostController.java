package Host.Library.Controller;

import Host.HostGui;
import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.GUI.Dialogs;
import Host.Storage;
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
    private HostGui gui;
    private HostConnector hostConnector = null;
    private HostConnector.Connection connection = null;

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
        try {
            gui = new HostGui(this, new Address(getOwnAddress(), HostConnector.DEFAULT_PORT));
            gui.reloadSheetTree(storage.getJTreeRoot());
        } catch (Address.BadIPAddressException e) {
            logger.severe("own address malformed\n" + e.toString() + "\nshutting down");
            System.exit(1);
        }
    }
/*--------------------------------------------------ConnectionLayer--------------------------------------------------*/

/*--------------------------------------------------------GUI--------------------------------------------------------*/
    public Storage.Info getFileInfo(TreePath path) throws Storage.FaultyStorageStructureException {
        return storage.getInfo(path);
    }

    public boolean listen(String password) {
        logger.info("listen(...)");
        if (hostConnector != null) {
            logger.warning("hostConnector already started");
            return true;
        }
        try {
            hostConnector = new HostConnector(this, prefs.getPort(), password);
            connection = hostConnector.startListening();
        } catch (IOException e) {
            logger.severe("listening failed\n" + e.toString() + "\nshutting down");
            e.printStackTrace();
            System.exit(1);
        }
        return hostConnector != null;
    }

    public void stopListen() {
        logger.info("stopListen(...)");
        hostConnector.stopListening();
    }

    public void disconnect() {
        logger.info("disconnect(...)");
        // TODO
        throw new UnimplementedException("Controller::connect(...) unimpl");
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

    private String getOwnAddress() {
        try {
            // TODO switch to external IP
            return ConnectionTools.getLocalIP();
        } catch (IOException e) {
            logger.warning("getIP failed\n" + e.toString());
            e.printStackTrace();
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

        public int getPort() {
            return port;
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
