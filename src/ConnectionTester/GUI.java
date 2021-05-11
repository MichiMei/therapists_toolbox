package ConnectionTester;

import Library.ConnectionLayer.ConnectionTools;
import Library.ContentPanes.Games.GRCFastRead;
import Library.ContentPanes.Games.GTCFastRead;
import Library.Protocol.*;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class GUI extends JFrame {
    private final ConnectionTester tester;

    public GUI(ConnectionTester tester) {
        this.tester = tester;
        completeReset();

        try {
            hostConnectIPTF.setText(ConnectionTools.getLocalIP());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        clientConnectButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> tester.connect(clientConnectIpTF.getText(), Integer.parseInt(clientConnectPortTF.getText())));
            connecting();
        });
        hostConnectButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> tester.listen(Integer.parseInt(hostConnectPortTF.getText())));
            connecting();
        });
        sendButton.addActionListener(e -> send());
        receiveButton.addActionListener(e -> receive());
        sendTypeSelector.addActionListener(e -> {
            switch (sendTypeSelector.getSelectedIndex()) {
                case 4, 5, 6, 7, 8, 9, 12 -> sendNoValueMessage();
                case 0, 1, 2, 3, 10 -> sendOneValueMessage();
                case 11 -> sendContentMessage();
            }
        });
        sendTypeSelector.setSelectedIndex(sendTypeSelector.getSelectedIndex());
        disconnectButton.addActionListener(e -> tester.disconnect());

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setSize(1000, 750);

        setContentPane(mainPanel);
        setVisible(true);
    }

    private void send() {
        sendButton.setEnabled(false);
        sendTypeSelector.setEnabled(false);
        sendContentTF.setEditable(false);
        sendTokenTF.setEditable(false);
        sendMillisTF.setEditable(false);
        System.out.println("hi1");
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() {
                System.out.println("hi2");
                Message msg = createMessage();
                System.out.println("hi3");
                if (msg == null) {
                    System.out.println("msg is null");
                    System.exit(-1);
                    return null;
                }
                System.out.println("hi4");
                tester.send(msg);
                System.out.println("hi5");
                return null;
            }
            @Override
            protected void done() {
                System.out.println("hi6");
                sendButton.setEnabled(true);
                sendTypeSelector.setEnabled(true);
                sendTypeSelector.setSelectedIndex(sendTypeSelector.getSelectedIndex());
            }
        }.execute();
    }

    private void receive() {
        receiveButton.setEnabled(false);
        new SwingWorker<Message, Object>() {
            @Override
            protected Message doInBackground() {
                return tester.receive();
            }
            @Override
            protected void done() {
                try {
                    Message msg = get();
                    parseMsg(msg);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    System.exit(-1);
                }
                receiveButton.setEnabled(true);
            }
        }.execute();
    }

    private void parseMsg(Message msg) {
        if (msg == null) {
            completeReset();
            return;
        }

        switch (msg.getType()) {
            case 0x0100 -> {
                receiveTypeTF.setText("0100:Close");
                receiveContentLabel.setText("ErrorCode");
                receiveContentTF.setText(""+((MCClose)msg.getContent()).getErrorCode());
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0101 -> {
                receiveTypeTF.setText("0101:Hello");
                receiveContentLabel.setText("Version");
                receiveContentTF.setText(""+((MCHello)msg.getContent()).getVersion());
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0102 -> {
                receiveTypeTF.setText("0102:HelloReply");
                receiveContentLabel.setText("pwRequired");
                receiveContentTF.setText("" + ((MCHelloReply)msg.getContent()).isPwRequired());
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0103 -> {
                receiveTypeTF.setText("0103:Registration");
                receiveContentLabel.setText("Password");
                receiveContentTF.setText(((MCRegistration)msg.getContent()).getPassword());
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0104 -> {
                receiveTypeTF.setText("0104:RegistrationAccept");
                receiveContentLabel.setText("");
                receiveContentTF.setText("");
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0200, 0x0201, 0x0202, 0x0203 -> {
                receiveTypeTF.setText("02xx:WorkSheet");
                receiveContentLabel.setText("");
                receiveContentTF.setText("");
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x03000 -> {
                receiveTypeTF.setText("0300:GameEnd");
                receiveContentLabel.setText("");
                receiveContentTF.setText("");
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0301 -> {
                receiveTypeTF.setText("0301:GameStart");
                receiveContentLabel.setText("GameID");
                receiveContentTF.setText(""+((MCGameStart)msg.getContent()).getGameID());
                receiveGameContentLabel.setText("");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
            case 0x0302 -> {
                receiveTypeTF.setText("0302:GameTransmit");
                receiveContentLabel.setText("");
                receiveContentTF.setText("");
                receiveGameContentLabel.setText("GTFastRead");
                receiveTokenLabel.setText("Token");
                receiveTokenTF.setText(""+((GTCFastRead)((MCGameTransmit)msg.getContent()).getContent()).getToken());
                receiveMillisLabel.setText("Millis");
                receiveMillisTF.setText(""+((GTCFastRead)((MCGameTransmit)msg.getContent()).getContent()).getMillis());
            }
            case 0x0303 -> {
                receiveTypeTF.setText("0303:GameReply");
                receiveContentLabel.setText("");
                receiveContentTF.setText("");
                receiveGameContentLabel.setText("GRFastRead");
                receiveTokenLabel.setText("");
                receiveTokenTF.setText("");
                receiveMillisLabel.setText("");
                receiveMillisTF.setText("");
            }
        }
    }

    private Message createMessage() {
        MessageContent content;
        switch (sendTypeSelector.getSelectedIndex()) {
            case 0 -> content = new MCClose(Integer.parseInt(sendContentTF.getText()));
            case 1 -> content = new MCHello(Integer.parseInt(sendContentTF.getText()));
            case 2 -> {
                System.out.println("hi7");
                int b = Integer.parseInt(sendContentTF.getText());
                System.out.println("hi8");
                boolean pwReq = (b & 0b10000000) != 0;
                System.out.println("hi9");
                content = new MCHelloReply(pwReq);
                System.out.println("hi10");
            }
            case 3 -> content = new MCRegistration(sendContentTF.getText());
            case 4 -> content = new MCRegistrationAccept();
            case 9 -> content = new MCGameEnd();
            case 10 -> content = new MCGameStart(Integer.parseInt(sendContentTF.getText()));
            case 11 -> content = new MCGameTransmit(new GTCFastRead(sendTokenTF.getText(), Integer.parseInt(sendMillisTF.getText())));
            case 12 -> content = new MCGameReply(new GRCFastRead());
            default -> {return null;}
        }
        return new Message(content.getType(), content);
    }

    private void connecting() {
        clientConnectButton.setEnabled(false);
        hostConnectButton.setEnabled(false);
        clientConnectIpTF.setEditable(false);
        clientConnectPortTF.setEditable(false);
        hostConnectButton.setEnabled(false);
    }

    public void connected() {
        sendButton.setEnabled(true);
        receiveButton.setEnabled(true);
        sendTypeSelector.setEnabled(true);
        disconnectButton.setEnabled(true);
    }

    private void completeReset() {
        disconnectButton.setEnabled(false);
        clientConnectButton.setEnabled(true);
        clientConnectButton.setText("Connect");
        hostConnectButton.setEnabled(true);
        hostConnectButton.setText("Connect");
        clientConnectIpTF.setEditable(true);
        clientConnectIpTF.setText("");
        clientConnectPortTF.setEditable(true);
        clientConnectPortTF.setText("");
        hostConnectPortTF.setEditable(true);
        hostConnectPortTF.setText("");

        sendButton.setEnabled(false);
        sendTypeSelector.setEnabled(false);
        sendContentLabel.setText("");
        sendContentTF.setText("");
        sendContentTF.setEditable(false);
        sendGameContentLabel.setText("");
        sendTokenLabel.setText("");
        sendTokenTF.setText("");
        sendTokenTF.setEditable(false);
        sendMillisLabel.setText("");
        sendMillisTF.setText("");
        sendMillisTF.setEditable(false);

        receiveButton.setEnabled(false);
        receiveTypeTF.setText("");
        receiveContentLabel.setText("");
        receiveContentTF.setText("");
        receiveGameContentLabel.setText("");
        receiveTokenLabel.setText("");
        receiveTokenTF.setText("");
        receiveMillisLabel.setText("");
        receiveMillisTF.setText("");
    }

    private void sendNoValueMessage() {
        sendContentLabel.setText("");
        sendContentTF.setText("");
        sendGameContentLabel.setText("");
        sendTokenLabel.setText("");
        sendTokenTF.setText("");
        sendMillisLabel.setText("");
        sendMillisTF.setText("");
    }

    private void sendOneValueMessage() {
        switch (sendTypeSelector.getSelectedIndex()) {
            case 0 -> sendContentLabel.setText("ErrorCode (int)");
            case 1 -> sendContentLabel.setText("version (int)");
            case 2 -> sendContentLabel.setText("flags (byte)");
            case 3 -> sendContentLabel.setText("password (String)");
            case 10 -> sendContentLabel.setText("gameID (int)");
        }
        sendContentTF.setEditable(true);
        sendGameContentLabel.setText("");
        sendTokenLabel.setText("");
        sendTokenTF.setText("");
        sendTokenTF.setEditable(false);
        sendMillisLabel.setText("");
        sendMillisTF.setText("");
        sendMillisTF.setEditable(false);
    }

    private void sendContentMessage() {
        sendContentLabel.setText("");
        sendContentTF.setEditable(false);
        sendGameContentLabel.setText("TransmitContent");
        sendTokenLabel.setText("Token (String)");
        sendTokenTF.setEditable(true);
        sendMillisLabel.setText("Millis (int)");
        sendMillisTF.setEditable(true);
    }

    private JComboBox<String> sendTypeSelector;
    private JTextField sendContentTF;
    private JTextField sendTokenTF;
    private JTextField sendMillisTF;
    private JButton sendButton;
    private JButton receiveButton;
    private JTextField clientConnectIpTF;
    private JTextField hostConnectPortTF;
    private JTextField clientConnectPortTF;
    private JButton clientConnectButton;
    private JButton hostConnectButton;
    private JTextField receiveTypeTF;
    private JLabel sendContentLabel;
    private JLabel receiveContentLabel;
    private JTextField receiveContentTF;
    private JLabel sendGameContentLabel;
    private JLabel receiveGameContentLabel;
    private JLabel sendTokenLabel;
    private JLabel receiveTokenLabel;
    private JTextField receiveTokenTF;
    private JLabel sendMillisLabel;
    private JLabel receiveMillisLabel;
    private JTextField receiveMillisTF;
    private JPanel mainPanel;
    private JButton disconnectButton;
    private JTextField hostConnectIPTF;
}
