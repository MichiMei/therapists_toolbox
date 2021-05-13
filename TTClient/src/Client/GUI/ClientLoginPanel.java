package Client.GUI;

import Client.Controller.ClientController;
import ConnectionLayer.Address;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Login Panel
 * Allows user inputs to connect to a host
 */
public class ClientLoginPanel extends JPanel {
    private final Logger logger;

/*------------------------------------------------------MAIN_GUI------------------------------------------------------*/

    /**
     * Create Login Panel
     * @param controller Controller reference to forward user inputs
     */
    public ClientLoginPanel(ClientController controller, String defaultAddress) {
        super();
        this.logger = Logger.getLogger(ClientLoginPanel.class.getName());

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("Resources/StringLiterals");

        this.addressTextField.setText(defaultAddress);

        connectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressTextField.setEditable(false);
                passwordField.setEditable(false);
                connectButton.setEnabled(false);
                String address = addressTextField.getText();
                if (address == null) {
                    statusTextField.setText(resourceBundle.getString("address_wrong"));
                    addressTextField.setEditable(true);
                    passwordField.setEditable(true);
                    connectButton.setEnabled(true);
                    return;
                }
                statusTextField.setText(resourceBundle.getString("connecting"));
                // try to connect
                new SwingWorker<Integer, Object>() {
                    @Override
                    protected Integer doInBackground() {
                        return controller.connect(address, new String(passwordField.getPassword()));
                    }

                    @Override
                    protected void done() {
                        try {
                            Integer res = get();
                            if (res == null) {
                                logger.severe("Could not get return value\nshutting down");
                                System.exit(-1);
                            }
                            switch (res) {
                                case 0 -> {
                                    statusTextField.setText(resourceBundle.getString("connected"));
                                    addressTextField.setEditable(true);
                                    passwordField.setEditable(true);
                                    connectButton.setEnabled(true);
                                }
                                case -1 -> {
                                    statusTextField.setText(resourceBundle.getString("connection_failed"));
                                    addressTextField.setEditable(true);
                                    passwordField.setEditable(true);
                                    connectButton.setEnabled(true);
                                }
                                case -2 -> {
                                    statusTextField.setText(resourceBundle.getString("password_wrong_client_side"));
                                    addressTextField.setEditable(true);
                                    passwordField.setEditable(true);
                                    connectButton.setEnabled(true);
                                }
                                case -3 -> {
                                    statusTextField.setText(resourceBundle.getString("host_closed"));
                                    addressTextField.setEditable(true);
                                    passwordField.setEditable(true);
                                    connectButton.setEnabled(true);
                                }
                                case -4 -> {
                                    statusTextField.setText(resourceBundle.getString("host_protocol_violation"));
                                    addressTextField.setEditable(true);
                                    passwordField.setEditable(true);
                                    connectButton.setEnabled(true);
                                }
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
    }

/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Testing purposes
     * Creates just this SUB-GUI -> many features won't work!
     * @param args not parsed
     */
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new ClientLoginPanel(null, ""));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    /**
     * Transform string address into object
     * @param text address as String IPv4:Port or [IPv6]:Port
     * @return Object representation of the String
     */
    private Address parseAddress (String text) {
        try {
            return new Address(text);
        } catch (Address.BadIPAddressException e) {
            logger.warning("could not parse ip address\n" + e.getMessage());
            return null;
        }
    }


    private JTextField addressTextField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JTextField statusTextField;
    private JPanel mainPanel;
}
