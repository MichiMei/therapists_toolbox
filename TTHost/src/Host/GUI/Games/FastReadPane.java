package Host.GUI.Games;

import Host.Controller.Games.FastReadController;
import Host.Controller.Games.GameControllerCreator;
import Host.CustomLogger;
import ContentPanes.Components.ImprovedFormattedTextField.ImprovedFormattedTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class FastReadPane extends GamePanel {
    private static final int ID = 0;

    // references
    private final ContentPanes.Games.FastReadPane previewPane;
    private final FastReadController controller;
    private final CustomLogger logger;

    // separator attributes
    private final Separator[] separators = new Separator[4];
    private int currentSeparatorIndex;

    // status attributes
    private boolean autoDisplay = false;
    private boolean editMode = false;

    // token attributes
    private String content = null;
    private int tokenStart = -1;
    private int tokenEnd = -1;
    private String currentToken = null;

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setExtendedState(JFrame.MAXIMIZED_BOTH);
        window.setContentPane(new FastReadPane(null));
        window.setVisible(true);
    }

    public FastReadPane(GameControllerCreator.GameController con) {
        super();
        this.controller = (FastReadController) con;
        logger = CustomLogger.getInstance();

        separators[0] = new Separator("whitespaces", Character::isWhitespace);
        separators[1] = new Separator("spaces", Character::isSpaceChar);
        separators[2] = new Separator("semicolon", c -> c.equals(';'));
        separators[3] = new Separator("pipe", c -> c.equals('|'));

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        setEditMode(true);

        NumberFormat integerNumberInstance = NumberFormat.getNumberInstance();
        integerNumberInstance.setGroupingUsed(false);
        integerNumberInstance.setParseIntegerOnly(true);
        timeSelector = new ImprovedFormattedTextField(integerNumberInstance, 1);
        timeSelector.addPropertyChangeListener("value", evt -> {
            System.out.println("changed: " + evt.getNewValue());
            System.out.println(evt.getNewValue().getClass());
        });
        timeSelectorPanel.add(timeSelector, BorderLayout.CENTER);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("Resources/StringLiterals");
        try {
            for (Separator sep : separators) {
                separatorComboBox.addItem(resourceBundle.getString(sep.resourceName));
            }
            currentSeparatorIndex = separatorComboBox.getSelectedIndex();
        } catch (MissingResourceException ex) {
            // TODO
            System.err.println("missing resource");
            System.exit(1);
        }

        editButton.setSelected(true);
        editButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setEditMode(!editMode);
            }
        });

        separatorComboBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentSeparatorIndex = separatorComboBox.getSelectedIndex();
                assert (currentSeparatorIndex >= 0);
                assert (currentSeparatorIndex < separators.length);
            }
        });

        nextButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextToken();
            }
        });

        previousButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prevToken();
            }
        });

        displayButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //int millis = Integer.parseInt(timeSelector.getText());
                int millis = 0;
                try {
                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    millis = numberFormat.parse(timeSelector.getText()).intValue();
                } catch (ParseException ex) {
                    logger.severe("parsing millis failed\n" + ex.getMessage() + "\nshutting down");
                    ex.printStackTrace();
                    System.exit(-1);
                }

                int finalMillis = millis;
                SwingUtilities.invokeLater(() -> previewPane.display(finalMillis, null));
                statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/Resources/Icons/red-circle.png")));
                displayButton.setEnabled(false);
                SwingUtilities.invokeLater(() -> controller.display(currentToken, finalMillis));
            }
        });

        autoDisplayCheckBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoDisplay = autoDisplayCheckBox.isSelected();
            }
        });

        endButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(controller::end);
            }
        });

        previewPane = new ContentPanes.Games.FastReadPane(true);
        previewPane.setBorder(new LineBorder(Color.GRAY, 1, true));
        previewPanel.add(previewPane, BorderLayout.CENTER);

        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "arrow_right");
        this.getActionMap().put("arrow_right", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (nextButton.isEnabled()) nextButton.doClick();
            }
        });
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "arrow_left");
        this.getActionMap().put("arrow_left", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (previousButton.isEnabled()) previousButton.doClick();
            }
        });
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space");
        this.getActionMap().put("space", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editMode == false) displayButton.requestFocus();
                if (displayButton.isEnabled()) displayButton.doClick();
            }
        });
        this.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "enter");
        this.getActionMap().put("enter", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (editMode == false) displayButton.requestFocus();
            }
        });

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    public void displayFinished() {
        statusLabel.setIcon(new ImageIcon(this.getClass().getResource("/Resources/Icons/green-circle.png")));
        displayButton.setEnabled(true);
    }

    private void setEditMode(boolean b) {
        editMode = b;
        if (b) {    // change to edit-mode
            displayButton.setEnabled(false);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            textArea.setEnabled(true);
            separatorComboBox.setEnabled(true);

            content = null;
            tokenStart = -1;
            tokenEnd = -1;
            currentToken = null;
            removeHighlight();
        } else {    // change to present-mode
            content = textArea.getText();
            if (checkContent(separators[currentSeparatorIndex].isDelimiter)) {
                displayButton.setEnabled(true);
                previousButton.setEnabled(true);
                nextButton.setEnabled(true);
                textArea.setEnabled(false);
                separatorComboBox.setEnabled(false);

                nextToken();
            } else {
                System.out.println("no printable output");
            }
        }
    }

    private void highlight() {
        Highlighter highlighter = textArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
        highlighter.removeAllHighlights();
        try {
            highlighter.addHighlight(tokenStart, tokenEnd, painter);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
            // TODO
        }
    }

    private void removeHighlight() {
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
    }

    private void nextToken() {
        do {
            tokenStart = tokenEnd + 1;
            if (tokenStart >= content.length()) tokenStart = 0;
            tokenEnd = nextIndexOf(content, separators[currentSeparatorIndex].isDelimiter, tokenStart);
            currentToken = content.substring(tokenStart, tokenEnd).trim();
        } while (currentToken.length() <= 0);

        // TODO sent setToken(currentToken)
        previewPane.setToken(currentToken);
        highlight();
        if (autoDisplay) displayButton.doClick();
    }

    private void prevToken() {
        do {
            tokenEnd = tokenStart - 1;
            if (tokenEnd <= 0) tokenEnd = content.length();
            tokenStart = prevIndexOf(content, separators[currentSeparatorIndex].isDelimiter, tokenEnd);
            currentToken = content.substring(tokenStart, tokenEnd).trim();
        } while (currentToken.length() <= 0);

        // TODO sent setToken(currentToken)
        previewPane.setToken(currentToken);
        highlight();
        if (autoDisplay) displayButton.doClick();
    }

    private int nextIndexOf(String string, Function<Character, Boolean> isDelim, int start) {
        for (; start < string.length(); start++) {
            if (isDelim.apply(string.charAt(start))) {
                return start;
            }
        }
        return string.length();
    }

    private int prevIndexOf(String string, Function<Character, Boolean> isDelim, int end) {
        end--;
        for (; end >= 0; end--) {
            if (isDelim.apply(string.charAt(end))) {
                return end+1;
            }
        }
        return 0;
    }

    private boolean checkContent(Function<Character, Boolean> isDelim) {
        for (int i = 0; i < content.length(); i++) {
            char tmp = content.charAt(i);
            if (!Character.isWhitespace(tmp) && !isDelim.apply(tmp)) return true;
        }
        return false;
    }

    private JButton displayButton;
    private JButton previousButton;
    private JButton nextButton;
    private JCheckBox autoDisplayCheckBox;
    private JToggleButton editButton;
    private JComboBox<String> separatorComboBox;
    private JPanel mainPanel;
    private JTextArea textArea;
    private JPanel timeSelectorPanel;
    private JPanel previewPanel;
    private JButton endButton;
    private JLabel statusLabel;

    private final ImprovedFormattedTextField timeSelector;

    @Override
    public int getID() {
        return ID;
    }

    private static class Separator {
        public Separator(String resourceName, Function<Character, Boolean> isDelimiter) {
            this.resourceName = resourceName;
            this.isDelimiter = isDelimiter;
        }

        public String resourceName;
        public Function<Character, Boolean> isDelimiter;
    }
}
