package Client.Library.Controller;

import Client.ClientGui;
import Client.Library.ConnectionLayer.ClientConnector;
import Host.Library.Controller.HostController;
import Library.ConnectionLayer.Address;
import Library.ContentClasses.UnimplementedException;

import java.io.IOException;
import java.util.logging.Logger;

public class ClientController {

    private static final int VERSION = 0x00010001;

    private final Logger logger;
    private ClientGui gui;
    private ClientConnector.Connection connection = null;

    public static void main(String[] args) {
        new ClientController();
    }

    public ClientController() {
        logger = Logger.getLogger(ClientController.class.getName());

        // create GUI
        gui = new ClientGui(this);

    }

/*--------------------------------------------------ConnectionLayer--------------------------------------------------*/

/*--------------------------------------------------------GUI--------------------------------------------------------*/

    public boolean connect(Address address, String password) {
        try {
            connection = new ClientConnector(address, password, VERSION).connect();
        } catch (IOException e) {
            logger.severe("connecting failed\n" + e.toString() + "\nshutting down");
            e.printStackTrace();
            System.exit(1);
        }

        if (connection != null) {
            gui.connected();
            return true;
        } else {
            return false;
        }
    }

}
