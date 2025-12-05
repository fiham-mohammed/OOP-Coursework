package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ParticipantTest {
    private Participant participant;

    @BeforeEach
    void setUp() {
        participant = new Participant("P101", "John Doe", "john@email.com",
                "Valorant", 8, "Attacker", 4, 5, 3, 4, 5, 85, "Balanced");
    }

    @Test
    void testConstructorAndGetters() {
        assertEquals("P101", participant.getId());
        assertEquals("John Doe", participant.getName());
        assertEquals("john@email.com", participant.getEmail());
        assertEquals("Valorant", participant.getInterest());
        assertEquals(8, participant.getSkillLevel());
        assertEquals("Attacker", participant.getRole());
        assertEquals(84, participant.getPersonalityScore());
        assertEquals("Balanced", participant.getPersonalityType());
    }

    @Test
    void testHasFiveQuestions() {
        assertTrue(participant.hasFiveQuestions());
    }

    @Test
    void testComputeTotalFromQuestions() {
        assertEquals(21, participant.computeTotalFromQuestions());
    }

    @Test
    void testClassifyPersonality() {
        assertEquals("Leader", participant.classifyPersonality(95));
        assertEquals("Balanced", participant.classifyPersonality(75));
        assertEquals("Thinker", participant.classifyPersonality(60));
        assertEquals("Undefined", participant.classifyPersonality(40));
    }

    @Test
    void testIsValid() {
        assertTrue(participant.isValid());
    }

    @Test
    void testToCSVForParticipant() {
        String csv = participant.toCSVForParticipant();
        assertTrue(csv.contains("P101"));
        assertTrue(csv.contains("John Doe"));
        assertTrue(csv.contains("Valorant"));
    }
}