package Host.Library.GUI;

import Library.ContentPanes.Components.ImprovedFormattedTextField.ImprovedFormattedTextField;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.function.Function;

public class HostFastReadPane extends JPanel {
    private boolean autoDisplay = false;
    private boolean editMode = false;
    private final Separator[] separators = new Separator[4];
    private int currentSeparatorIndex;

    private String content = null;
    private int tokenStart = -1;
    private int tokenEnd = -1;
    private String currentToken = null;

    private static class Separator {
        public Separator(String resourceName, Function<Character, Boolean> isDelimiter) {
            this.resourceName = resourceName;
            this.isDelimiter = isDelimiter;
        }

        public String resourceName;
        public Function<Character, Boolean> isDelimiter;
    }

    public static void main(String[] args) {
        JFrame window = new JFrame();
        window.setContentPane(new HostFastReadPane());
        window.setVisible(true);
    }

    public HostFastReadPane () {
        super();

        separators[0] = new Separator("whitespaces", Character::isWhitespace);
        separators[1] = new Separator("spaces", Character::isSpaceChar);
        separators[2] = new Separator("semicolon", c -> c.equals(';'));
        separators[3] = new Separator("pipe", c -> c.equals('|'));

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        setEditMode(true);

        NumberFormat integerNumberInstance = NumberFormat.getNumberInstance();
        integerNumberInstance.setParseIntegerOnly(true);
        timeSelector = new ImprovedFormattedTextField(integerNumberInstance, 1);
        timeSelector.addPropertyChangeListener("value", evt -> {
            System.out.println("changed: " + evt.getNewValue());
            System.out.println(evt.getNewValue().getClass());
        });
        timeSelectorPanel.add(timeSelector, BorderLayout.CENTER);

        ResourceBundle resourceBundle = ResourceBundle.getBundle("Library/Resources/StringLiterals");
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
                // TODO tell communication layer to display
                System.out.println("Displaying <" + currentToken + ">");
            }
        });

        autoDisplayCheckBox.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoDisplay = autoDisplayCheckBox.isSelected();
            }
        });

        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
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

        System.out.println("Next token:");
        System.out.println("start: " + tokenStart);
        System.out.println("end  : " + tokenEnd);
        System.out.println("token:<" + currentToken + ">\n");
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

        System.out.println("Previous token:");
        System.out.println("start: " + tokenStart);
        System.out.println("end  : " + tokenEnd);
        System.out.println("token:<" + currentToken + ">\n");
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

    private final ImprovedFormattedTextField timeSelector;


}
