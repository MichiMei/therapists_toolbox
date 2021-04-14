package Client.Library.GUI;

import Client.Library.Controller.ClientController;

import javax.swing.*;

public class ClientGui extends JFrame {
    private final ClientController controller;

    public static void main(String[] args) {
        new ClientGui(null);
    }

    public ClientGui(ClientController controller) {
        super("Therapists-Toolbox Client");
        this.controller = controller;

        // WINDOW //
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setBounds(0,0,1000,750);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            System.err.println("Setting 'LookAndFeel' to native style failed");
            e1.printStackTrace();
        }

        // Content
        ClientLoginPane loginPane = new ClientLoginPane(controller);
        setContentPane(loginPane);

        setVisible(true);
    }

    public void connected() {
        // TODO
        //throw new UnimplementedException("ClientGui::connected() unimpl");
    }

}
