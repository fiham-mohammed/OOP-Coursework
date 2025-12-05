package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

class TestFileManager {
    private FileManager fileManager;

    @BeforeEach
    void setUp() {
        fileManager = new FileManager();
    }

    @Test
    void testFormTeams() {
        List<Participant> testParticipants = new ArrayList<>();
        testParticipants.add(new Participant("P101", "Test1", "test1@email.com",
                "Valorant", 5, "Attacker", 3, 4, 3, 4, 3, 80, "Balanced"));
        testParticipants.add(new Participant("P102", "Test2", "test2@email.com",
                "Dota", 6, "Strategist", 4, 5, 4, 4, 4, 85, "Thinker"));

        List<Team> teams = fileManager.formTeams(testParticipants, 2);

        assertNotNull(teams);
        assertFalse(teams.isEmpty());
    }

    @Test
    void testFormTeamsWithEmptyList() {
        List<Team> teams = fileManager.formTeams(new ArrayList<>(), 4);
        assertNotNull(teams);
        assertTrue(teams.isEmpty());
    }
}