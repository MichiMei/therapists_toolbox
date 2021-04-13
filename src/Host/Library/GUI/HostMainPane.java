package Host.Library.GUI;

import Host.Library.Controller.HostController;
import Host.Storage;
import Library.ConnectionLayer.Address;
import Library.ContentClasses.BadPageException;
import Library.ContentClasses.Page;
import Library.ContentClasses.SingleChoiceTextQuestionPage;
import Library.ContentClasses.UnimplementedException;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

public class HostMainPane extends JPanel {

    private final HostController controller;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new HostMainPane(null, null));
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

/*-----------------------------------------------------Controller-----------------------------------------------------*/

    /**
     * Creates a new Host Main (Navigation) Pane
     * @param controller Controller reference for user input delegation
     */
    public HostMainPane (HostController controller, Address address) {
        super();
        this.controller = controller;

        workSheetsPanel.add(new JLabel(new ImageIcon(this.getClass().getResource("/Library/Resources/Icons/loading.gif"))));

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        addressTextField.setText(address.toString());

        onlineToggleButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (onlineToggleButton.isSelected()) {
                    passwordCheckBox.setEnabled(false);
                    passwordTextField.setEditable(false);
                    statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("going_online"));
                    // go online
                    String pw;
                    if (passwordCheckBox.isSelected()) {
                        pw = passwordTextField.getText();
                    } else {
                        pw = null;
                    }
                    new SwingWorker<Boolean, Object>() {
                        @Override
                        protected Boolean doInBackground() {
                            return controller.listen(pw);
                        }

                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    onlineToggleButton.setEnabled(false);
                                    disconnectButton.setEnabled(true);
                                    statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("connected"));
                                } else {
                                    passwordCheckBox.setEnabled(true);
                                    passwordTextField.setEditable(true);
                                    onlineToggleButton.setSelected(false);
                                    statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("offline"));
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                // TODO
                            }
                        }
                    }.execute();

                } else {
                    passwordCheckBox.setEnabled(true);
                    passwordTextField.setEditable(true);
                    statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("offline"));
                    onlineToggleButton.setEnabled(false);
                    // go offline
                    //SwingUtilities.invokeLater(controller::stopListen);

                    new SwingWorker<>() {
                        @Override
                        protected Object doInBackground() {
                            controller.stopListen();
                            return null;
                        }

                        @Override
                        protected void done() {
                            onlineToggleButton.setEnabled(true);
                        }
                    }.execute();
                }
            }
        });

        disconnectButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                disconnectButton.setEnabled(false);
                statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("disconnecting"));
                SwingUtilities.invokeLater(controller::disconnect);
            }
        });

        startButton.addActionListener(  // TODO
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String[] answers = {"Yes", "Maybe", "Probably", "No", "Stop"};
                        Page page = null;
                        try {
                            page = new SingleChoiceTextQuestionPage("What?", "U don't understand...", answers, 3, true);
                        } catch (BadPageException badPageException) {
                            badPageException.printStackTrace();
                            System.exit(1);
                        }
                        JPanel panel = page.createPanel();
                        JFrame window = new JFrame("test");
                        window.add(panel, BorderLayout.CENTER);
                        window.setVisible(true);
                    }
                }
        );


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
                new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
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
                }
        );
        workSheetsPanel.removeAll();
        workSheetsPanel.add(tree, BorderLayout.CENTER);
    }

    /**
     * Display connected status
     */
    public void connected() {
        onlineToggleButton.setEnabled(false);
        disconnectButton.setEnabled(true);
        statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("connected"));
    }

    /**
     * Display offline status
     */
    public void disconnected() {
        onlineToggleButton.setSelected(false);
        onlineToggleButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        passwordCheckBox.setEnabled(true);
        passwordTextField.setEditable(true);
        statusFormattedTextField.setText(ResourceBundle.getBundle("Library/Resources/StringLiterals").getString("offline"));
    }

/*------------------------------------------------------PRIVATE------------------------------------------------------*/

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
    private JPanel editingPanel;
    private JButton newDirButton;
    private JLabel Directory;
    private JButton deleteDirButton;
    private JButton newFileButton;
    private JButton editFileButton;
    private JButton deleteFileButton;
    private JButton editDirButton;
    private JTabbedPane tabbedPane1;
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
    private JScrollPane consoleScrollPane;
    private JTextPane consoleTextPane;
    private JSplitPane splitPane;
    private JTree sheetSelectionTree;
    private JPanel workSheetsPanel;
    private JPanel gamesPanel;
    private JButton fastReadButton;

}
