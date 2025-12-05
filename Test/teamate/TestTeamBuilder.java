package teamate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class TestTeamBuilder {
    private List<Participant> participants;

    @BeforeEach
    void setUp() {
        participants = new ArrayList<>();

        participants.add(new Participant("P101", "Leader1", "leader1@email.com",
                "Valorant", 8, "Attacker", 5, 5, 5, 5, 5, 100, "Leader"));
        participants.add(new Participant("P102", "Thinker1", "thinker1@email.com",
                "Dota", 7, "Strategist", 4, 5, 4, 4, 5, 88, "Thinker"));
        participants.add(new Participant("P103", "Balanced1", "balanced1@email.com",
                "FIFA", 6, "Defender", 4, 4, 4, 4, 4, 75, "Balanced"));
    }

    @Test
    void testTeamBuilderConstructor() {
        TeamBuilder teamBuilder = new TeamBuilder(participants, 3);
        assertNotNull(teamBuilder);
    }

    @Test
    void testFormAllTeamsWithValidParticipants() {
        TeamBuilder teamBuilder = new TeamBuilder(participants, 3);
        Map<String, Object> result = teamBuilder.formAllTeams();

        assertNotNull(result);
        assertTrue(result.containsKey("wellBalanced"));
        assertTrue(result.containsKey("secondary"));
        assertTrue(result.containsKey("leftover"));
    }

    @Test
    void testFormAllTeamsWithEmptyList() {
        TeamBuilder teamBuilder = new TeamBuilder(new ArrayList<>(), 4);
        Map<String, Object> result = teamBuilder.formAllTeams();

        List<Team> wellBalanced = (List<Team>) result.get("wellBalanced");
        List<Team> secondary = (List<Team>) result.get("secondary");
        List<Participant> leftover = (List<Participant>) result.get("leftover");

        assertTrue(wellBalanced.isEmpty());
        assertTrue(secondary.isEmpty());
        assertTrue(leftover.isEmpty());
    }
}