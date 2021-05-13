package Host.GUI;

import Exceptions.BadGameIDException;
import Host.ConnectionLayer.HostConnector;
import Host.Controller.Games.GameControllerCreator;
import Host.Controller.HostController;
import Host.GUI.Games.FastReadPane;
import Host.GUI.Games.GamePanel;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ResourceBundle;

public class HostGui extends JFrame {

    private final HostController controller;
    private final ResourceBundle resources;

    // panes
    private final HostMainPane hostMainPane;
    private GamePanel gamePane = null;

/*-----------------------------------------------------CONTROLLER-----------------------------------------------------*/

    /**
     * Create new MainGui
     * @param controller controller reference for user-input notification
     */
    public HostGui(HostController controller) {
        super("Therapists-Toolbox Host");
        this.controller = controller;
        this.resources = ResourceBundle.getBundle("Resources/StringLiterals");
        JFrame window = this;

        // WINDOW //
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) { }
            @Override
            public void windowClosing(WindowEvent e) {
                controller.exit();
            }
            @Override
            public void windowClosed(WindowEvent e) { }
            @Override
            public void windowIconified(WindowEvent e) { }
            @Override
            public void windowDeiconified(WindowEvent e) { }
            @Override
            public void windowActivated(WindowEvent e) { }
            @Override
            public void windowDeactivated(WindowEvent e) { }
        });
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e1) {
            System.err.println("Setting 'LookAndFeel' to native style failed");
            e1.printStackTrace();
        }

        // MENU // // TODO
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

        // CONTENT //
        hostMainPane = new HostMainPane(controller);
        setContentPane(hostMainPane);

        this.setVisible(true);
    }

    /**
     * Reload FileSystem Tree representation after changes (e.g. new File or Folder)
     * @param rootNode root of the directory tree
     */
    public void reloadSheetTree(DefaultMutableTreeNode rootNode) {
        hostMainPane.reloadSheetTree(rootNode);
        repaint();
        revalidate();
    }

    /**
     * Notify Gui of connection status changes to disply correct state
     * @param status status of the connection
     * @param msg special message to display
     */
    public void connectionStatusChanged(HostConnector.Status status, String msg) {
        hostMainPane.connectionStatusChanged(status, msg);
    }

    public GamePanel startedGame(int id, GameControllerCreator.GameController controller) throws BadGameIDException {
        switch (id) {
            case 0:
                gamePane = new FastReadPane(controller);
                setContentPane(gamePane);
                revalidate();
                repaint();
                return gamePane;
            default:
                throw new BadGameIDException(id);
        }
    }

    public void endedGame() {
        gamePane = null;
        setContentPane(hostMainPane);
        revalidate();
        repaint();
    }

}
