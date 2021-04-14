package Client.Library.Controller;

import Client.Library.GUI.ClientGui;
import Client.Library.ConnectionLayer.ClientConnector;
import Library.ConnectionLayer.Address;

import java.io.IOException;
import java.util.logging.Logger;

public class ClientController {

    private static final int VERSION = 0x00010001;

    private final Logger logger;
    private final ClientGui gui;
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

    /**
     *
     * @param address
     * @param password
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
            gui.connected();
            return 0;
        } else {
            return -1;
        }
    }

}
