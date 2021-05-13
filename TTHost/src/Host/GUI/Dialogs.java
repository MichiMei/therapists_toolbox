package Host.GUI;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class Dialogs {

    /**
     * Let's the user choose weather to use a new or existing location
     *
     * @return true iff new location; false iff existing location
     * @throws CancelPressedException thrown iff user presses cancel (or close)
     */
    public static boolean showStartDialog() throws CancelPressedException {
        ResourceBundle resources = ResourceBundle.getBundle("Resources/StringLiterals");

        String title = resources.getString("select_storage");
        String message = resources.getString("first_start_msg");
        String[] options = {
                resources.getString("new"),
                resources.getString("existing"),
                resources.getString("cancel"),
        };
        int result = JOptionPane.showOptionDialog(
                null,                         // the parent that the dialog blocks
                message,                                    // the dialog message array
                title,                                // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,                 // option type
                JOptionPane.INFORMATION_MESSAGE,            // message type
                null,                                  // optional icon, use null to use the default icon
                options,                                    // options string array, will be made into buttons
                options[0]                                  // option that should be made into a default button
        );
        return switch (result) {
            case 0 -> true;     // New
            case 1 -> false;    // Existing
            default -> throw new CancelPressedException("src/Host/GUI/Dialogs.java: showStartDialog(...) cancel pressed");
        };
    }

    public static void developerDialog(String message) {
        String title = "Developer Dialog";
        StringBuilder msg = new StringBuilder("<html><P><font color=red>This is an information for the developer only.<br>If this message is shown to a user it can be ignored.<br><br></font></P><P><font color=black>");
        msg.append(message.replace("\n", "<br>"));
        msg.append("</font></P></html>");
        String[] options = {"OK"};
        JOptionPane.showOptionDialog(
                null,
                msg,
                title,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    /**
     * Let's the user choose the directory for the storage
     *
     * @param defaultPath default path for the storage
     * @return the user selected Path for the storage
     * @throws CancelPressedException thrown iff User cancels the file-chooser
     */
    public static Path chooseLocation(Path defaultPath) throws CancelPressedException {
        // create file-chooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (defaultPath != null) fileChooser.setCurrentDirectory(defaultPath.toFile());
        int result = fileChooser.showDialog(null, "Select");
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().toPath();
        } else {
            throw new CancelPressedException("User canceled File-choosing");
        }
    }

    public static class CancelPressedException extends Exception {
        public CancelPressedException(String message) {
            super(message);
        }
    }

    public static void aboutDialog(Component parent) {
        ResourceBundle resources = ResourceBundle.getBundle("Resources/StringLiterals");

        String title = resources.getString("about") + " " + resources.getString("therapists_toolbox");
        String message = resources.getString("about_message_host");
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
