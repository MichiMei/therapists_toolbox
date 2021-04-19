package Client.Library.GUI;

import Client.Library.Controller.Games.GameControllerCreator;
import Client.Library.Controller.ClientController;
import Library.BadGameIDException;
import Library.ContentPanes.Games.FastReadPane;
import Library.ContentPanes.Games.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * Clients Main GUI
 * Mainly controlling different sub-GUIs for different purposes
 */
public class ClientGui extends JFrame {
    private final ClientController controller;
    private Library.ContentPanes.Games.GamePanel gamePanel = null;
    private final ResourceBundle resources;


/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create a main GUI
     * @param controller needs Controller reference for notifications
     */
    public ClientGui(ClientController controller) {
        super("Therapists-Toolbox Client");
        this.resources = ResourceBundle.getBundle("Library/Resources/StringLiterals");
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
        JMenu menuHelp = new JMenu(resources.getString("help"));
        menuBar.add(menuHelp);
        JMenuItem menuItemAbout = new JMenuItem(resources.getString("about"));
        menuHelp.add(menuItemAbout);
        menuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Dialogs.aboutDialog(window);
            }
        });

        // Content
        ClientLoginPane loginPane = new ClientLoginPane(controller);
        setContentPane(loginPane);

        setVisible(true);
    }

    /**
     * Notify GUI of established connection to switch panes
     * (LoginPane to WaitingPane)
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

    public GamePanel startedGame(int id, GameControllerCreator.GameController controller) throws BadGameIDException {
        switch (id) {
            case 0:
                gamePanel = new FastReadPane(controller);
                setContentPane(gamePanel);
                revalidate();
                repaint();
                return gamePanel;
            default:
                throw new BadGameIDException(id);
        }
    }

    public void ended() {
        gamePanel = null;
        connected();
    }

    public void disconnected() {
        gamePanel = null;
        ClientLoginPane loginPane = new ClientLoginPane(controller);
        setContentPane(loginPane);
        revalidate();
        repaint();
    }

/*------------------------------------------------------SUB_GUIS------------------------------------------------------*/

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Testing purposes
     * Creates just the GUI -> many features won't work!
     * @param args
     */
    public static void main(String[] args) {
        new ClientGui(null);
    }

}
