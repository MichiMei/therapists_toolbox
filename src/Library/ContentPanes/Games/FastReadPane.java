package Library.ContentPanes.Games;

import Client.Library.Controller.Games.FastReadController;
import Client.Library.Controller.Games.GameControllerCreator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

public class FastReadPane extends GamePanel {

    private final boolean previewMode;
    private String token = null;
    private final FastReadController controller;

    public static void main(String[] args) throws InterruptedException {
        JFrame window = new JFrame();
        FastReadPane panel = new FastReadPane(null);
        window.setContentPane(panel);
        window.setVisible(true);

        Thread.sleep(2000);
        panel.setText("This is far longer");
    }

    public FastReadPane(GameControllerCreator.GameController controller) {
        super();
        this.previewMode = controller == null;
        this.controller = (FastReadController) controller;

        textLabel.setHorizontalAlignment(JLabel.CENTER);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Font old = textLabel.getFont();
                textLabel.setFont(new Font(old.getName(), old.getStyle(), getMaxFontSize(textLabel.getText())));
            }
        });

        this.setLayout(new BorderLayout());
        this.add(panel1, BorderLayout.CENTER);
    }

    public void setToken(String token) {
        this.token = token;
        if (previewMode) {
            textLabel.setForeground(Color.GRAY);
            setText(token);
        }
    }

    public void display(int millis) {
        textLabel.setForeground(Color.BLACK);
        setText(token);
        new SwingWorker<>() {
            @Override
            protected Object doInBackground() throws Exception {
                Thread.sleep(millis);
                return null;
            }
            @Override
            protected void done() {
                if (previewMode) {
                    textLabel.setForeground(Color.GRAY);
                } else {
                    setText("");
                    controller.displayFin();
                }
            }
        }.execute();
    }




    private void setText(String text) {
        textLabel.setText(text);
        Font old = textLabel.getFont();
        int fontSize = getMaxFontSize(text);
        textLabel.setFont(new Font(old.getName(), old.getStyle(), fontSize));
        System.out.println("set '" + text + "' with " + fontSize);
        //setMaxFontSize();
    }

    private int getMaxFontSize(String str) {
        int fontSize = 1;
        if (str == null || str.length() == 0) return fontSize;
        double prevWidth = 0;
        double prevHeight = 0;
        while (true) {
            Font old = textLabel.getFont();
            Font font = new Font(old.getName(), old.getStyle(), fontSize+1);
            FontRenderContext frc = new FontRenderContext(font.getTransform(), false, false);
            TextLayout layout = new TextLayout(str, font, frc);
            Rectangle2D rect = layout.getBounds();
            double width = rect.getWidth();
            double height = rect.getHeight();
            if (width > textLabel.getWidth() || height > textLabel.getWidth()) {
                System.out.println("exceeded");
                break;
            }
            if (prevWidth == width || prevHeight == height) {
                System.out.println("no change");
                break;
            }
            prevHeight = height; prevWidth = width;
            fontSize++;
        }
        return fontSize*3/4;
    }

    private void setMaxFontSize() {
        Font labelFont = textLabel.getFont();
        String labelText = textLabel.getText();

        int stringWidth = textLabel.getFontMetrics(labelFont).stringWidth(labelText);
        int componentWidth = textLabel.getWidth();
//        System.out.println("Label Width: " + componentWidth);

        // Find out how much the font can grow in width.
        double widthRatio = (double)componentWidth / (double)stringWidth;

        int newFontSize = (int)(0.5 * labelFont.getSize() * widthRatio);
        int componentHeight = (int)(0.5 * textLabel.getHeight());
//        System.out.println("Label Height: " + textLabel.getHeight());

        // Pick a new font size so it will not be larger than the height of label.
        int fontSizeToUse = Math.min(newFontSize, componentHeight);

        // Set the label's font size to the newly determined size.
        textLabel.setFont(new Font(labelFont.getName(), Font.PLAIN, fontSizeToUse));
    }

    private JPanel panel1;
    private JLabel textLabel;
}
