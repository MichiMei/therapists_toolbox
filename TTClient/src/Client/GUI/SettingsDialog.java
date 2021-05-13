package Client.GUI;

import Client.Controller.ClientController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton okButton;
    private JButton cancelButton;
    private JButton applyButton;
    private JTextField portTextField;
    private JTextField addressTextField;
    private JSlider fontSizeSlider;
    private JTextArea sampleTextTextArea;

    private final ClientController controller;
    private int prevPort;

    public SettingsDialog(ClientController controller) {
        this.controller = controller;
        new SettingsDialog(controller, new Dimension(300, 200));
    }

    public SettingsDialog(ClientController controller, Dimension dim) {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(okButton);
        this.controller = controller;
        this.setSize(dim);

        this.prevPort = controller.getPort();
        portTextField.setText("" + prevPort);
        addressTextField.setText(controller.getAddress());
        fontSizeSlider.setValue(controller.getDefFontSize());

        okButton.addActionListener(e -> onOK());

        applyButton.addActionListener(e -> onApply());

        cancelButton.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        fontSizeSlider.addChangeListener(e -> {
            Font oldFont = sampleTextTextArea.getFont();
            Font newFont = new Font(oldFont.getName(), oldFont.getStyle(), 12*fontSizeSlider.getValue()/100);
            sampleTextTextArea.setFont(newFont);
        });

        portTextField.addActionListener(e -> {
            try {
                int newPort = Integer.parseInt(portTextField.getText());
                if (newPort < 1024 || newPort > 65535) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                portTextField.setText("" + prevPort);
            }
        });

        this.setVisible(true);
    }

    private void onOK() {
        onApply();
        dispose();
    }

    private void onApply() {
        controller.setPort(Integer.parseInt(portTextField.getText()));
        controller.setDefFontSize(fontSizeSlider.getValue());
        controller.setAddress(addressTextField.getText());
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        SettingsDialog dialog = new SettingsDialog(null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
