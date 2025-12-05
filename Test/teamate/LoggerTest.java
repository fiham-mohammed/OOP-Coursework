package teamate;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    @Test
    void testSingletonInstance() {
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();

        assertSame(logger1, logger2, "Logger should be singleton");
    }

    @Test
    void testLogLevels() {
        Logger logger = Logger.getInstance(false);

        assertDoesNotThrow(() -> logger.debug("Debug message"));
        assertDoesNotThrow(() -> logger.info("Info message"));
        assertDoesNotThrow(() -> logger.warn("Warning message"));
        assertDoesNotThrow(() -> logger.error("Error message"));
    }

    @Test
    void testLogWithException() {
        Logger logger = Logger.getInstance(false);
        Exception testException = new RuntimeException("Test exception");

        assertDoesNotThrow(() -> logger.error("Error with exception", testException));
    }
}