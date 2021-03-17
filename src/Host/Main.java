package Host;

import Host.Library.GUI.Dialogs;
import Library.ContentClasses.UnimplementedException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class Main {

    private static final int port = 23432;
    private Preferences prefs;
    private HostGui hostApp;
    private Path storagePath;
    private Logger logger;
    private Storage storage;

    public static void main(String[] args) throws IOException {
        Main main = new Main();
        main.run();

//        // IP address stuff
//        String localIP = ConnectionTools.getLocalIP();
//        System.out.println("local IP: " + localIP);
//        String externalIP = ConnectionTools.getExternalIP();
//        System.out.println("external IP: " + externalIP);
//
//        // open listening socket
//        ServerSocket serverSocket = new ServerSocket(port);
//        logger.info("Listening on " + serverSocket.toString());
//
//        // listen on socket and accept
//        Socket clientSocket = serverSocket.accept();
//        logger.info("Connected to " + clientSocket.toString());
//
//        // create reader for accepted socket
//        BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//
//        // read line from reader
//        String line = clientReader.readLine();
//        logger.info("received: " + line);
//        System.out.println(line);
//
//        // create writer for accepted socket
//        PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);
//
//        // send response to client
//        clientWriter.println("received: " + line);
//
//        clientReader.close();
//        clientWriter.close();
//        clientSocket.close();
//        serverSocket.close();
    }

    private Main () throws IOException {
        logger = Logger.getLogger(Main.class.getName());
        logger.info("Start logging");
        prefs = Preferences.userNodeForPackage(getClass());
        readPreferences();
        loadStorage();
        hostApp = new HostGui(storage);
    }

    /**
     * Restores the Preferences set during the previous executions
     */
    private void readPreferences() {
        logger.info("get preferences...");
        String tmpPath = prefs.get("storagePath", null);
        if (tmpPath == null) {
            storagePath = null;
            logger.info("No storage path found");
        } else {
            storagePath = Paths.get(tmpPath);
            logger.info("Storage at: " + storagePath);
        }
        logger.info("all preferences loaded");
    }

    /**
     * Loads the storage
     * (if no storage is found, asks the user for the path to the new one to create or a existing one to check)
     */
    public void loadStorage() throws IOException {
        logger.info("loading storage...");

        while (storagePath == null) {   // first start
            try {
                boolean newLocation = Dialogs.showStartDialog();
                Path tmpPath = Dialogs.chooseLocation(Storage.defaultPath());
                if (newLocation) {
                    logger.info("creating new storage...");
                    storagePath = Storage.initStorage(tmpPath);
                    if (storagePath == null) {
                        // base-dir already existed
                        logger.warning("location already in use");
                        // TODO inform user, ask for new location or cancel
                        throw new UnimplementedException("src/Host/Main.java: loadStorage(...) is unimplemented");
                    }
                    prefs.put("storagePath", storagePath.toString());
                    logger.info("new storage created");
                } else {
                    logger.info("checking existing storage...");
                    storagePath = Storage.checkStorageSystem(tmpPath);
                    prefs.put("storagePath", storagePath.toString());
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

    private void run() throws UnimplementedException {
        // TODO
    }

}
