package Library.ContentPanes.Components;

import org.w3c.dom.css.Rect;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class AppendJTextPane extends JTextPane {

    public AppendJTextPane () {
        super();
    }

    public AppendJTextPane(StyledDocument doc) {
        super(doc);
    }

    // Appends text to the document and ensure that it is visible
    public void appendText(String text) {
        try {
            Document doc = getDocument();

            // append text (even if not editable)
            // TODO: add style options
            doc.insertString(doc.getLength(), text, null);

            // Convert the new end location
            // to view co-ordinates
            Rectangle r = modelToView(doc.getLength());

            // Finally, scroll so that the new text is visible
            if (r != null) {
                scrollRectToVisible(r);
            }
        } catch (BadLocationException e) {
            System.out.println("Failed to append text: " + e);
        }
    }

}
