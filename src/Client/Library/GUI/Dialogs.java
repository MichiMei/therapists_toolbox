package Client.Library.GUI;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class Dialogs {
    public static void aboutDialog(Component parent) {
        ResourceBundle resources = ResourceBundle.getBundle("Library/Resources/StringLiterals");

        String title = resources.getString("about") + " " + resources.getString("therapists_toolbox");
        String message = resources.getString("about_message_client");
        String[] options = {
                resources.getString("ok")
        };
        int result = JOptionPane.showOptionDialog(
                parent,                         // the parent that the dialog blocks
                message,                                    // the dialog message array
                title,                                // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,                 // option type
                JOptionPane.INFORMATION_MESSAGE,            // message type
                null,                                  // optional icon, use null to use the default icon
                options,                                    // options string array, will be made into buttons
                options[0]                                  // option that should be made into a default button
        );
    }
}
