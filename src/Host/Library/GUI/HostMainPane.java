package Host.Library.GUI;

import Host.Storage;
import Library.ContentClasses.BadPageException;
import Library.ContentClasses.Page;
import Library.ContentClasses.SingleChoiceTextQuestionPage;
import Library.ContentClasses.UnimplementedException;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class HostMainPane extends JPanel {

    private final Storage storage;

    public HostMainPane (Storage storage) {
        super();

        this.storage = storage;
        splitPane.setLeftComponent(createJTree());

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        startButton.addActionListener(
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

    private JTree createJTree() {
        DefaultTreeModel model = new DefaultTreeModel(storage.getJTreeRoot());
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
                            Storage.Info info = storage.getInfo(path);
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
        return tree;
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
                        // TODO show Folder editing Dialo
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
    private JTree sheetSelectionTree;
    private JPanel editingPanel;
    private JButton newDirButton;
    private JLabel Directory;
    private JButton deleteDirButton;
    private JButton newFileButton;
    private JButton editFileButton;
    private JButton deleteFileButton;
    private JButton editDirButton;
    private JTabbedPane tabbedPane1;
    private JFormattedTextField adressTextField;
    private JCheckBox passwordCheckBox;
    private JTextField passwordTextField;
    private JFormattedTextField statusFormattedTextField;
    private JButton offlineButton;
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

}
