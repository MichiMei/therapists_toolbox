package Host;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class CustomLogger {
    private final Logger logger;
    private static final CustomLogger instance = new CustomLogger();

    private CustomLogger() {
        logger = Logger.getAnonymousLogger();
    }

    public static CustomLogger getInstance() {
        return instance;
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void severe(String msg) {
        logger.severe(msg);
    }

    public void addHandler(FileHandler fh) {
        logger.addHandler(fh);
    }

    public void closeHandlers() {
        for (Handler handler : logger.getHandlers()) {
            handler.close();
        }
    }
}
