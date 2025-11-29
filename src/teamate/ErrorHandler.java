package teamate;

public class ErrorHandler {
    private final Logger logger = Logger.getInstance();

    public void showError(String m){
        System.err.println("[ERROR] " + m);
        logger.error("UI Error: " + m);
    }

    public void showInfo(String m){
        System.out.println("[INFO] " + m);
        logger.info("UI Info: " + m);
    }

    public void logError(String message) {
        logger.error(message);
    }

    public void logError(String message, Exception e) {
        logger.error(message, e);
    }

    public void logInfo(String message) {
        logger.info(message);
    }

    public void logWarning(String message) {
        logger.warn(message);
    }

    public void logDebug(String message) {
        logger.debug(message);
    }
}