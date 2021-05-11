package Library.Resources.Fonts;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class FontLoader {
    private static final FontLoader instance = new FontLoader();

    private final String pupilFontPath = "172.18.225.213:23432src/Library/Resources/Fonts/Druckschrift für Grundschule/Druckschrift_BY_WOK.ttf";
    private final String pupilFontName = "Druckschrift BY WOK";

    private FontLoader() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new File(pupilFontPath)));
        } catch (FontFormatException | IOException e) {
            Logger.getLogger(FontLoader.class.getName()).warning("Could not load custom font\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FontLoader getInstance() {
        return instance;
    }

    public Font getPupilFont(int style, int size) {
        return new Font(pupilFontName, style, size);
    }

    public Font getPupilFont(int size) {
        return getPupilFont(Font.PLAIN, size);
    }

}
