package Client.GUI;

import Client.Controller.Games.GameControllerCreator;
import Client.Controller.ClientController;
import Library.BadGameIDException;
import Library.ContentPanes.Games.FastReadPane;
import Library.ContentPanes.Games.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Clients Main GUI
 * Mainly controlling different sub-GUIs for different purposes
 */
public class ClientGui extends JFrame {
    private final ClientController controller;
    private Library.ContentPanes.Games.GamePanel gamePanel = null;


    /*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create a main GUI
     * @param controller needs Controller reference for notifications
     */
    public ClientGui(ClientController controller) {
        super("Therapists-Toolbox Client");
        ResourceBundle resources = ResourceBundle.getBundle("Library/Resources/StringLiterals");
        this.controller = controller;
        JFrame window = this;

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

        // MENU
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu menuData = new JMenu(resources.getString("data"));
        menuBar.add(menuData);
        JMenuItem menuItemSettings = new JMenuItem(resources.getString("settings"));
        menuData.add(menuItemSettings);
        menuItemSettings.addActionListener(e -> controller.displaySettings(new Dimension(window.getWidth()/2, window.getHeight()/2)));

        JMenu menuHelp = new JMenu(resources.getString("help"));
        menuBar.add(menuHelp);
        JMenuItem menuItemAbout = new JMenuItem(resources.getString("about"));
        menuHelp.add(menuItemAbout);
        menuItemAbout.addActionListener(e -> Dialogs.aboutDialog(window));

        // Content
        ClientLoginPanel loginPanel = new ClientLoginPanel(controller, controller.getAddress());
        setContentPane(loginPanel);

        setVisible(true);
    }

    /**
     * Notify GUI of established connection to switch panels
     * (LoginPanel to WaitingPanel)
     */
    public void connected() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("please_wait"));
        Font font = new Font(label.getFont().getName(), label.getFont().getStyle(), label.getFont().getSize()*2);
        label.setFont(font);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        setContentPane(panel);
        revalidate();
        repaint();
    }

    /**
     * Game started
     * Switch Gui to game
     * @param id game id
     * @param controller game specific controller (for game specific gui)
     * @return game specific gui
     * @throws BadGameIDException thrown if game with given id does not exist
     */
    public GamePanel startedGame(int id, GameControllerCreator.GameController controller) throws BadGameIDException {
        switch (id) {
            case 0 -> {
                gamePanel = new FastReadPane(controller);
                setContentPane(gamePanel);
                revalidate();
                repaint();
                return gamePanel;
            }
            default -> throw new BadGameIDException(id);
        }
    }

    /**
     * Game ended
     * Switch Gui
     */
    public void ended() {
        gamePanel = null;
        connected();
    }

    /**
     * Connection disconnected
     * Return to Login Panel
     */
    public void disconnected() {
        gamePanel = null;
        ClientLoginPanel loginPanel = new ClientLoginPanel(controller, controller.getAddress());
        setContentPane(loginPanel);
        revalidate();
        repaint();
    }

/*------------------------------------------------------SUB_GUIS------------------------------------------------------*/

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Testing purposes
     * Creates just the GUI -> many features won't work!
     * @param args not parsed
     */
    public static void main(String[] args) {
        new ClientGui(null);
    }

}
