package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestTeam {
    private Team team;
    private Participant participant1;

    @BeforeEach
    void setUp() {
        team = new Team(1);
        participant1 = new Participant("P101", "John Doe", "john@email.com",
                "Valorant", 8, "Attacker", 4, 5, 3, 4, 5, 85, "Balanced");
    }

    @Test
    void testTeamCreation() {
        assertEquals(1, team.getTeamID());
        assertEquals(0, team.size());
        assertTrue(team.getMembers().isEmpty());
    }

    @Test
    void testAddMember() {
        team.addMember(participant1);
        assertEquals(1, team.size());
        assertEquals(participant1, team.getMembers().get(0));
    }

    @Test
    void testToCSVLines() {
        team.addMember(participant1);
        String csv = team.toCSVLines();
        assertTrue(csv.contains("1,P101"));
        assertTrue(csv.contains("John Doe"));
    }
}