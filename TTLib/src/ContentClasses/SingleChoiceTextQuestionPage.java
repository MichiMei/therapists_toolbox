package ContentClasses;

import ContentPanes.WorkSheets.SingleChoiceTextQuestionPane;

import javax.swing.*;

public class SingleChoiceTextQuestionPage implements Page {

    private final String question;
    private final String text; // TODO allow format
    private final String[] answers;
    private final int correctAnswer;
    private final boolean randomizeAnswers;

    public SingleChoiceTextQuestionPage(String question, String text, String[] answers, int correctAnswer,
                                        boolean randomizeAnswers) throws BadPageException {
        if (correctAnswer < 0) {
            throw new BadPageException("SCTQ: correctAnswer (" + correctAnswer + ") < 1");
        }
        if (correctAnswer >= answers.length) {
            throw new BadPageException("SCTQ: correctAnswer (" + correctAnswer + ") >= answers.length (" + answers.length + ")");
        }
        this.question = question;
        this.text = text;
        this.answers = answers;
        this.correctAnswer = correctAnswer;
        this.randomizeAnswers = randomizeAnswers;
    }

    public String getQuestion() {
        return question;
    }

    public String getText() {
        return text;
    }

    public String[] getAnswers() {
        return answers;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public boolean randomizeAnswers() {
        return randomizeAnswers;
    }

    public int getAnswerCount() {
        return answers.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SCTQ(");
        sb.append("question:").append(this.question).append("; ");
        sb.append("text:").append(this.text).append("; ");
        sb.append("correctAnswer:").append(correctAnswer).append("; ");
        sb.append("randomizeAnswers:").append(randomizeAnswers).append("; ");
        sb.append("answers[");
        for (int i = 0; i < answers.length; i++) {
            sb.append("answer").append(i).append(":").append(answers[i]).append("; ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public JPanel createPanel() {
        return new SingleChoiceTextQuestionPane(this);
    }
}
