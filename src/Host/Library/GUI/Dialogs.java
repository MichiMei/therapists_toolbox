package Host.Library.GUI;

import javax.swing.*;
import java.nio.file.Path;

public class Dialogs {

    /**
     * Let's the user choose weather to use a new or existing location
     *
     * @return true iff new location; false iff existing location
     * @throws CancelPressedException thrown iff user presses cancel (or close)
     */
    public static boolean showStartDialog() throws CancelPressedException {
        String title = "Select storage location";
        String message = "<html><P><font color=black>The program was started for the first time, a location for the data storage needs to be selected.<br></font></P><P><br>If this or a previous version of this program was already used, <br>You can select the ‘Existing’ location to import the old data.<br>(The storage location contains a folder called ‘TherapistsToolbox’)<br></P><P><br>Otherwise choose ‘New’ to select a new location.</P></html>";
        String[] options = {
                "New",
                "Existing",
                "Cancel",
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
            default -> throw new CancelPressedException("src/Host/Library/GUI/Dialogs.java: showStartDialog(...) cancel pressed");
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
}
