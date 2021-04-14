package Host.Library.GUI;

import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.Controller.HostController;
import Host.Library.Controller.Storage;
import Library.ConnectionLayer.Address;
import Library.ContentClasses.UnimplementedException;

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
                SwingUtilities.invokeLater(controller::disconnect);
                disableConnectionInput();
            }
        });

        startButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO
                throw new UnimplementedException("HostMainPane::startButton::actionListener unimpl");
            }
        });

    }

    /**
     * Display (current) Work Sheet Directory Tree
     * @param rootNode rootNode of the Directory Tree
     */
    public void reloadSheetTree(DefaultMutableTreeNode rootNode) {
        DefaultTreeModel model = new DefaultTreeModel(rootNode);
        model.setAsksAllowsChildren(true);
        JTree tree = new JTree(model);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.getSelectionModel().addTreeSelectionListener(
                e -> {
                    TreePath path = e.getNewLeadSelectionPath();
                    if (path == null) {
                        String msg = "null-element selected in JTree\n";
                        msg += "check if anything is selected\n";
                        msg += "previous path:\n";
                        msg += e.getOldLeadSelectionPath().toString();
                        msg += "\n";
                        Dialogs.developerDialog(msg);
                        return;
                    }
                    try {
                        Storage.Info info = controller.getFileInfo(path);
                        infoNameTextField.setText(info.getName());
                        infoDirTextField.setText(info.getPath());
                        if (info.isDirectory()) {
                            infoDescriptionTextField.setText("");
                            infoVersionTextField1.setText("");
                            infoPagesTextField.setText("");
                            newDirButton.setEnabled(true);
                            editDirButton.setEnabled(true);
                            deleteDirButton.setEnabled(true);
                            newFileButton.setEnabled(false);
                            editFileButton.setEnabled(false);
                            deleteFileButton.setEnabled(false);
                        } else {
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
                    } catch (Storage.FaultyStorageStructureException ex) {
                        ex.printStackTrace();
                        // TODO
                        throw new UnimplementedException("");
                    }
                }
        );
        workSheetsPanel.removeAll();
        workSheetsPanel.add(tree, BorderLayout.CENTER);
    }

    public void connectionStatusChanged(HostConnector.Status status, String msg) {
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
                startButton.setEnabled(true);
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
    private JButton fastReadButton;

}
