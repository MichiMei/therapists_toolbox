package Host.Library.GUI;

import Host.Library.ConnectionLayer.HostConnector;
import Host.Library.Controller.HostController;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class HostGui extends JFrame {

    private final HostController controller;

    // panes
    HostMainPane hostMainPane;

    public HostGui(HostController controller) {
        super("Therapists-Toolbox Host");
        this.controller = controller;

        // WINDOW //
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        JMenu menuData = new JMenu("Data");
        menuBar.add(menuData);
        JMenuItem menuItemReset = new JMenuItem("Reset window sizes");
        menuData.add(menuItemReset);
//        menuItemReset.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                resetWindows();
//            }
//        });
        JMenu menuSettings = new JMenu("Settings");
        menuBar.add(menuSettings);
        JCheckBoxMenuItem menuItemSubfolderMode = new JCheckBoxMenuItem("Include sub-folders", false);
        menuSettings.add(menuItemSubfolderMode);
//        menuItemSubfolderMode.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                subfolderMode = menuItemSubfolderMode.getState();
//                reloadPP();
//            }
//        });

        // CONTENT //
        hostMainPane = new HostMainPane(controller);
        setContentPane(hostMainPane);

        this.setVisible(true);
    }

    public void reloadSheetTree(DefaultMutableTreeNode rootNode) {
        hostMainPane.reloadSheetTree(rootNode);
        repaint();
        revalidate();
    }

    public void connectionStatusChanged(HostConnector.Status status, String msg) {
        hostMainPane.connectionStatusChanged(status, msg);
    }

}
