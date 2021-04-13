package Client.Library.GUI;

import Client.Library.Controller.ClientController;
import Host.Library.GUI.HostMainPane;
import Library.ConnectionLayer.Address;
import Library.ContentClasses.UnimplementedException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

public class ClientLoginPane  extends JPanel {

    private ClientController controller;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new ClientLoginPane(null));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

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
                // TODO try to connect
                new SwingWorker<Boolean, Object>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return controller.connect(address, new String(passwordField.getPassword()));
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                statusTextField.setText("");
                                addressTextField.setEditable(true);
                                passwordField.setEditable(true);
                                connectButton.setEnabled(true);
                            } else {
                                statusTextField.setText(resourceBundle.getString("connection_failed"));
                                addressTextField.setEditable(true);
                                passwordField.setEditable(true);
                                connectButton.setEnabled(true);
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }.execute();
            }
        });
    }

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
