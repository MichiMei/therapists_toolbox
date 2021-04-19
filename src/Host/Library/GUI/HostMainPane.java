package Host.Library.GUI;

import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.Controller.Games.GameControllerCreator;
import Host.Library.Controller.HostController;
import Host.Library.Controller.Storage;
import Library.BadGameIDException;
import Library.ConnectionLayer.Address;
import Library.UnimplementedException;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

public class HostMainPane extends JPanel {

    private final HostController controller;
    private final ResourceBundle resources;
    private final Logger logger;
    private JTree sheetsTree;
    private HostConnector.Status connectionStatus;
    private JRadioButton[] gameButtons;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new HostMainPane(null));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

/*-----------------------------------------------------Controller-----------------------------------------------------*/

    /**
     * Creates a new Host Main (Navigation) Pane
     * @param controller Controller reference for user input delegation
     */
    public HostMainPane (HostController controller) {
        super();
        this.controller = controller;
        this.resources = ResourceBundle.getBundle("Library/Resources/StringLiterals");
        this.logger = Logger.getLogger(HostMainPane.class.getName());

        workSheetsPanel.add(new JLabel(new ImageIcon(this.getClass().getResource("/Library/Resources/Icons/loading.gif"))));

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        onlineToggleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onlineToggleButton.isSelected()) {
                    // switch to online
                    String pw;
                    if (passwordCheckBox.isSelected()) {
                        pw = passwordTextField.getText();
                        if (pw == null || pw.equals("")) {
                            consoleTextPane.setText(resources.getString("insert_password") + "\n" + consoleTextPane.getText());
                            return;
                        }
                    } else {
                        pw = null;
                    }
                    statusFormattedTextField.setText(resources.getString("going_online"));

                    new SwingWorker<Boolean, Object>() {
                        @Override
                        protected Boolean doInBackground() {
                            return controller.goOnline(pw);
                        }
                        @Override
                        protected void done() {
                            try {
                                Boolean result = get();
                                assert (result != null);
                                if (!result) {
                                    consoleTextPane.setText(resources.getString("check_internet") + "\n" + consoleTextPane.getText());
                                    connectionStatusChanged(HostConnector.Status.Offline, null);
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                logger.severe("Swing Worker done exception\n" + e.toString() + "\nshutting down");
                                e.printStackTrace();
                                System.exit(1);
                            }
                        }
                    }.execute();
                } else {
                    // switch to offline
                    statusFormattedTextField.setText(resources.getString("going_offline"));
                    SwingUtilities.invokeLater(controller::goOffline);
                }
                disableConnectionInput();
            }
        });

        disconnectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disableConnectionInput();
                SwingUtilities.invokeLater(controller::disconnect);
            }
        });

        startButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.info("start pressed");
                switch (sheetGamesTabbedPane.getSelectedIndex()) {
                    case 0:
                        controller.startWorkSheet(sheetsTree.getSelectionPath());
                        break;
                    case 1:
                        int id = -1;
                        for (int i = 0; i < gameButtons.length; i++) {
                            if (gameButtons[i].isSelected()) {
                                id = i;
                                break;
                            }
                        }
                        assert(id != -1);
                        logger.info("selected game: " + id);
                        controller.startGame(id);
                    default:
                }
            }
        });

        sheetGamesTabbedPane.addChangeListener(e -> displayInfo());

        gamesPanel.setLayout(new BoxLayout(gamesPanel, BoxLayout.Y_AXIS));
        ButtonGroup gamesRadioButtons = new ButtonGroup();
        gameButtons = new JRadioButton[1];  // TODO modify for several games
        for (int i = 0; i < 1; i++) {       // TODO modify for several games
            JRadioButton rb = new JRadioButton(resources.getString("fast_read"));
            gamesPanel.add(rb);
            gamesRadioButtons.add(rb);
            gameButtons[i] = rb;
            rb.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    displayInfo();
                }
            });
        }
    }

    /**
     * Display (current) Work Sheet Directory Tree
     * @param rootNode rootNode of the Directory Tree
     */
    public void reloadSheetTree(DefaultMutableTreeNode rootNode) {
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        model.setAsksAllowsChildren(true);
        sheetsTree = new JTree(model);
        sheetsTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        sheetsTree.getSelectionModel().addTreeSelectionListener(
                e -> {
                    displayInfo();
                }
        );
        workSheetsPanel.removeAll();
        workSheetsPanel.add(sheetsTree, BorderLayout.CENTER);
    }

    /**
     * Called to notify GUI of a connection status change
     * @param status current connection status
     * @param msg console message to display
     */
    public void connectionStatusChanged(HostConnector.Status status, String msg) {
        this.connectionStatus = status;
        disableConnectionInput();
        switch (status) {
            case Online -> {
                onlineToggleButton.setEnabled(true);
                onlineToggleButton.setText(resources.getString("online"));
                onlineToggleButton.setSelected(true);
                statusFormattedTextField.setText(resources.getString("waiting_for_client"));
                Address own = controller.getAddress();
                assert (own != null);
                addressTextField.setText(own.toString());
            }
            case Offline -> {
                onlineToggleButton.setEnabled(true);
                onlineToggleButton.setText(resources.getString("offline"));
                onlineToggleButton.setSelected(false);
                statusFormattedTextField.setText(resources.getString("offline"));
                addressTextField.setText("");
                passwordTextField.setEditable(true);
                passwordCheckBox.setEnabled(true);
            }
            case Connected -> {
                disconnectButton.setEnabled(true);
                displayInfo();  // enables start button, if fitting element selected
                statusFormattedTextField.setText(resources.getString("connected"));
            }
            case Connecting -> {
                disconnectButton.setEnabled(true);
                statusFormattedTextField.setText(resources.getString("connecting"));
            }
        }
        if (msg != null) consoleTextPane.setText(msg + "\n" + consoleTextPane.getText());
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

    /**
     * Called after change to work-sheet or game selection or tabbed-pane switch
     * Displays info about the selected file, folder or game
     */
    private void displayInfo() {
        int tpSelection = sheetGamesTabbedPane.getSelectedIndex();
        assert (tpSelection >= 0);
        assert (tpSelection < 2);
        if (tpSelection == 0) {     // worksheets selected
            displayFsElementInfo();
        } else {                    // games selected
            displayGameInfo();
        }
    }

    /**
     * Displays info about the selected file or folder
     */
    private void displayFsElementInfo() {
        assert (sheetGamesTabbedPane.getSelectedIndex() == 0);

        TreePath path = sheetsTree.getSelectionPath();
        if (path == null) {
            infoNameTextField.setText("");
            infoDirTextField.setText("");
            infoDescriptionTextField.setText("");
            infoVersionTextField1.setText("");
            infoPagesTextField.setText("");
            startButton.setEnabled(false);
            newDirButton.setEnabled(false);
            editDirButton.setEnabled(false);
            deleteDirButton.setEnabled(false);
            newFileButton.setEnabled(false);
            editFileButton.setEnabled(false);
            deleteFileButton.setEnabled(false);
        } else {
            try {
                Storage.Info info = controller.getFileInfo(path);
                infoNameTextField.setText(info.getName());
                infoDirTextField.setText(info.getPath());
                if (info.isDirectory()) {
                    infoDescriptionTextField.setText("");
                    infoVersionTextField1.setText("");
                    infoPagesTextField.setText("");
                    startButton.setEnabled(false);
                    newDirButton.setEnabled(true);
                    editDirButton.setEnabled(true);
                    deleteDirButton.setEnabled(true);
                    newFileButton.setEnabled(true);
                    editFileButton.setEnabled(false);
                    deleteFileButton.setEnabled(false);
                } else {
                    enableStartButton();
                    infoDescriptionTextField.setText(info.getDescription());
                    infoVersionTextField1.setText("" + info.getMinVersion());
                    infoPagesTextField.setText("" + info.getPages());
                    newDirButton.setEnabled(false);
                    editDirButton.setEnabled(false);
                    deleteDirButton.setEnabled(false);
                    newFileButton.setEnabled(true);
                    editFileButton.setEnabled(true);
                    deleteFileButton.setEnabled(true);
                }
            } catch (Storage.FaultyStorageStructureException e) {
                e.printStackTrace();
                // TODO
                throw new UnimplementedException("");
            }
        }
    }

    /**
     * Display info about the selected game
     */
    private void displayGameInfo() {
        startButton.setEnabled(false);
        newDirButton.setEnabled(false);
        editDirButton.setEnabled(false);
        deleteDirButton.setEnabled(false);
        newFileButton.setEnabled(false);
        editFileButton.setEnabled(false);
        deleteFileButton.setEnabled(false);

        int gameID = -1;
        for (int i = 0; i < gameButtons.length; i++) {
            if (gameButtons[i].isSelected()) {
                gameID = i;
                break;
            }
        }

        if (gameID == -1) {
            infoNameTextField.setText("");
            infoDirTextField.setText("");
            infoDescriptionTextField.setText("");
            infoVersionTextField1.setText("");
            infoPagesTextField.setText("");
        } else {
            try {
                enableStartButton();
                GameControllerCreator.Info info = controller.getGameInfo(gameID);
                infoNameTextField.setText(info.getName());
                infoDirTextField.setText("");
                infoDescriptionTextField.setText(info.getDescription());
                infoVersionTextField1.setText("");
                infoPagesTextField.setText("");
            } catch (BadGameIDException e) {
                e.printStackTrace();
                // TODO
                throw new UnimplementedException("");
            }
        }
    }

    /**
     * Enables start-button, if connected to a client
     */
    private void enableStartButton() {
        logger.info("enable start");
        if (connectionStatus == HostConnector.Status.Connected) {
            logger.info("enabled");
            startButton.setEnabled(true);
        }
    }

    private void disableConnectionInput() {
        onlineToggleButton.setEnabled(false);
        disconnectButton.setEnabled(false);
        startButton.setEnabled(false);
        passwordCheckBox.setEnabled(false);
        passwordTextField.setEditable(false);
    }

    private void setEditingButtonListeners() {
        newDirButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show Folder creation Dialog
                        // TODO default directory (currently selected folder or parent of file)
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
        newFileButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show File creation Dialog
                        // TODO default directory (currently selected folder or parent of file)
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
        editDirButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show Folder editing Dialog
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
        editFileButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show File editing Dialog
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
        deleteDirButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show confirmation Dialog
                        // TODO option delete all sub-folders and -files or merge into parent folder
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
        deleteFileButton.addActionListener(
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO show confirmation Dialog
                        // TODO perform changes in Storage
                        // TODO save storage
                    }
                }
        );
    }

    // components
    private JPanel mainPanel;
    private JButton newDirButton;
    private JButton deleteDirButton;
    private JButton newFileButton;
    private JButton editFileButton;
    private JButton deleteFileButton;
    private JButton editDirButton;
    private JTextField addressTextField;
    private JCheckBox passwordCheckBox;
    private JTextField passwordTextField;
    private JTextField statusFormattedTextField;
    private JToggleButton onlineToggleButton;
    private JButton disconnectButton;
    private JButton startButton;
    private JTextField infoVersionTextField1;
    private JTextField infoNameTextField;
    private JTextField infoDirTextField;
    private JTextField infoDescriptionTextField;
    private JTextField infoPagesTextField;
    private JTextPane consoleTextPane;
    private JPanel workSheetsPanel;
    private JTabbedPane sheetGamesTabbedPane;
    private JPanel gamesPanel;

}
