package ContentPanes.WorkSheets;

import ContentClasses.SingleChoiceTextQuestionPage;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SingleChoiceTextQuestionPane extends JPanel {

    public SingleChoiceTextQuestionPane (SingleChoiceTextQuestionPage page) {
        super();
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);

        // add question
        questionLabel.setText(page.getQuestion());

        // add text
        textTextPane.setText(page.getText());

        // create buttons
        answerButtons = new JButton[page.getAnswerCount()];
        String[] answers = page.getAnswers();
        for (int i = 0; i < answers.length; i++) {
            answerButtons[i] = new JButton(answers[i]);
        }

        // add buttons
        buttonPanel.setLayout(new GridLayout(0, 3));
        Iterator<JButton> iterator = Arrays.stream(answerButtons).iterator();
        if (page.randomizeAnswers()) {
            List<JButton> list = Arrays.asList(answerButtons);
            Collections.shuffle(list);
            iterator = list.iterator();
        }
        while (iterator.hasNext()) {
            JButton curr = iterator.next();
            System.out.println(curr.getText());
            buttonPanel.add(curr);
        }

    }

    // Components
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel questionPanel;
    private JPanel textPanel;
    private JLabel questionLabel;
    private JTextPane textTextPane;
    private JButton[] answerButtons;
}
