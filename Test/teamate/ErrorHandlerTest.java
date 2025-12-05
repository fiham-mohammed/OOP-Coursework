package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ErrorHandlerTest {
    private ErrorHandler errorHandler;

    @BeforeEach
    void setUp() {
        errorHandler = new ErrorHandler();
    }

    @Test
    void testShowError() {
        assertDoesNotThrow(() -> errorHandler.showError("Test error message"));
    }

    @Test
    void testShowInfo() {
        assertDoesNotThrow(() -> errorHandler.showInfo("Test info message"));
    }

    @Test
    void testLogErrorWithMessage() {
        assertDoesNotThrow(() -> errorHandler.logError("Simple error message"));
    }

    @Test
    void testLogErrorWithMessageAndException() {
        Exception testException = new RuntimeException("Test exception");
        assertDoesNotThrow(() -> errorHandler.logError("Error with exception", testException));
    }
}