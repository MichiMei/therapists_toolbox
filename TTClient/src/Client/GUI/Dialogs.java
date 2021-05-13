package Client.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * Dialog creator class
 */
public class Dialogs {

    /**
     * Display about dialog
     * @param parent parent component (blocks parent)
     */
    public static void aboutDialog(Component parent) {
        ResourceBundle resources = ResourceBundle.getBundle("Resources/StringLiterals");

        String title = resources.getString("about") + " " + resources.getString("therapists_toolbox");
        String message = resources.getString("about_message_client");
        String[] options = {
                resources.getString("ok")
        };
        showOptionDialog(parent, message, title, options, options[0]);
    }

    /**
     * Create and show arbitrary option dialogs
     * @param parent parent component (gets blocked)
     * @param message display message (html)
     * @param title display title
     * @param options selection options
     * @param defaultOption default option
     * @return returns index of selected object
     */
    private static int showOptionDialog(Component parent, String message, String title, Object[] options, Object defaultOption) {
        return JOptionPane.showOptionDialog(
                parent,                                 // the parent that the dialog blocks
                message,                                // the dialog message array
                title,                                  // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,             // option type
                JOptionPane.INFORMATION_MESSAGE,        // message type
                null,                              // optional icon, use null to use the default icon
                options,                                // options string array, will be made into buttons
                defaultOption                           // option that should be made into a default button
        );
    }
}
