package Host;

import Host.Library.GUI.HostMainPane;

import javax.swing.*;

public class HostGui extends JFrame {

    private JFrame window;
    private JPanel mainPanel;

    private Storage storage;

    public HostGui(Storage storage) {
        super("Therapists-Toolbox Host");
        this.storage = storage;

        // WINDOW //
        window = this;
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
        JCheckBoxMenuItem menuItemSubfolderMode = new JCheckBoxMenuItem("Include subfolders", false);
        menuSettings.add(menuItemSubfolderMode);
//        menuItemSubfolderMode.addItemListener(new ItemListener() {
//            @Override
//            public void itemStateChanged(ItemEvent e) {
//                subfolderMode = menuItemSubfolderMode.getState();
//                reloadPP();
//            }
//        });

        // CONTENT //
        HostMainPane mainPane = new HostMainPane(storage);
        setContentPane(mainPane);

//        mainPanel.setLayout(new BorderLayout());
//        final AppendJTextPane a = new AppendJTextPane();
//        a.setEditable(false);
//        JButton b = new JButton("test");
//        b.addActionListener(new AbstractAction() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                System.out.println("Button pressed");
//                a.appendText("hello\n");
//            }
//        });
//        mainPanel.add(b, BorderLayout.NORTH);
//        mainPanel.add(new JScrollPane(a), BorderLayout.CENTER);
//
//        setContentPane(mainPanel);

        this.setVisible(true);
    }

}
