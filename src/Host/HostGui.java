package Host;

import Host.Library.Controller.HostController;
import Host.Library.GUI.HostMainPane;
import Library.ConnectionLayer.Address;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class HostGui extends JFrame {

    private final HostController controller;

    // panes
    HostMainPane hostMainPane;

    public HostGui(HostController controller, Address address) {
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
        hostMainPane = new HostMainPane(controller, address);
        setContentPane(hostMainPane);

        this.setVisible(true);
    }

    public void reloadSheetTree(DefaultMutableTreeNode rootNode) {
        // TODO check if in correct state
        hostMainPane.reloadSheetTree(rootNode);
        repaint();
        revalidate();
    }

    public void connected() {
        // TODO check if in correct state
        hostMainPane.connected();
    }

    public void disconnected() {
        // TODO check if in correct state
        hostMainPane.disconnected();
    }

    private JPanel mainPanel;

}
