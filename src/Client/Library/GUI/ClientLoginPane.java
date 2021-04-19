package Client.Library.GUI;

import Client.Library.Controller.ClientController;
import Library.ConnectionLayer.Address;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

/**
 * Login Panel
 * Allows user inputs to connect to a host
 */
public class ClientLoginPane  extends JPanel {

    private final ClientController controller;

/*------------------------------------------------------MAIN_GUI------------------------------------------------------*/

    /**
     * Create Login Panel
     * @param controller Controller reference to forward user inputs
     */
    public ClientLoginPane(ClientController controller) {
        super();
        this.controller = controller;

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("Library/Resources/StringLiterals");

        connectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressTextField.setEditable(false);
                passwordField.setEditable(false);
                connectButton.setEnabled(false);
                Address address = parseAddress(addressTextField.getText());
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
                                // TODO
                                System.exit(1);
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
     * @param args
     */
    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new ClientLoginPane(null));
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
            e.printStackTrace();
            return null;
        }
    }


    private JTextField addressTextField;
    private JPasswordField passwordField;
    private JButton connectButton;
    private JTextField statusTextField;
    private JPanel mainPanel;
}
