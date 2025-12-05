package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestPersonalityClassifier {
    private PersonalityClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new PersonalityClassifier();
    }

    @Test
    void testClassifyLeader() {
        assertEquals("Leader", classifier.classify(90));
        assertEquals("Leader", classifier.classify(95));
        assertEquals("Leader", classifier.classify(100));
    }

    @Test
    void testClassifyBalanced() {
        assertEquals("Balanced", classifier.classify(70));
        assertEquals("Balanced", classifier.classify(75));
        assertEquals("Balanced", classifier.classify(89));
    }

    @Test
    void testClassifyThinker() {
        assertEquals("Thinker", classifier.classify(50));
        assertEquals("Thinker", classifier.classify(60));
        assertEquals("Thinker", classifier.classify(69));
    }

    @Test
    void testClassifyUndefined() {
        assertEquals("Undefined", classifier.classify(0));
        assertEquals("Undefined", classifier.classify(25));
        assertEquals("Undefined", classifier.classify(49));
    }
}