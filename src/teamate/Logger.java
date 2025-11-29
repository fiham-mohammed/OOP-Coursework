package teamate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Enhanced logging system with multiple levels and file output
 */
public class Logger {
    private static final String LOG_FILE = "teamate_system.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static Logger instance;
    private final boolean writeToFile;

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    private Logger(boolean writeToFile) {
        this.writeToFile = writeToFile;
        // Initialize log file with header
        if (writeToFile) {
            logToFile("=== TeamMate System Log Started ===");
        }
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger(true); // Enable file logging by default
        }
        return instance;
    }

    public static Logger getInstance(boolean writeToFile) {
        if (instance == null) {
            instance = new Logger(writeToFile);
        }
        return instance;
    }

    public void debug(String message) {
        log(Level.DEBUG, message);
    }

    public void info(String message) {
        log(Level.INFO, message);
    }

    public void warn(String message) {
        log(Level.WARN, message);
    }

    public void error(String message) {
        log(Level.ERROR, message);
    }

    public void error(String message, Exception e) {
        log(Level.ERROR, message + " - " + e.getMessage());
        if (e != null) {
            logToFile("Stack Trace: " + getStackTrace(e));
        }
    }

    private void log(Level level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] %s - %s", level, timestamp, message);

        // Console output
        switch (level) {
            case ERROR -> System.err.println(logMessage);
            case WARN -> System.out.println("\u001B[33m" + logMessage + "\u001B[0m"); // Yellow
            case INFO -> System.out.println("\u001B[32m" + logMessage + "\u001B[0m"); // Green
            default -> System.out.println(logMessage);
        }

        // File output
        if (writeToFile) {
            logToFile(logMessage);
        }
    }

    private void logToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}