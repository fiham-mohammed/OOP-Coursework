package teamate;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileManager reads CSV files and writes formed teams.
 * Supports either Q1..Q5 columns OR a PersonalityScore column.
 */
public class FileManager {
    private final Logger logger = Logger.getInstance();
    private String personalityType;

    // Reads participants from the given CSV path
    public List<Participant> readParticipantsFromCSV(String path) throws IOException {
        logger.debug("Reading participants from CSV: " + path);

        Path p = Paths.get(path);
        if (!Files.exists(p)) {
            logger.error("CSV file not found: " + path);
            throw new FileNotFoundException("CSV file not found: " + path);
        }

        List<Participant> list = new ArrayList<>();
        int lineCount = 0;
        int successCount = 0;

        try (BufferedReader br = Files.newBufferedReader(p)) {
            String header = br.readLine();
            if (header == null) {
                logger.warn("CSV file is empty: " + path);
                return list;
            }

            logger.debug("CSV header: " + header);
            String[] cols = header.split(",", -1);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < cols.length; i++) idx.put(cols[i].trim().toLowerCase(), i);

            String line;
            int ln = 1;
            while ((line = br.readLine()) != null) {
                ln++;
                lineCount++;
                if (line.trim().isEmpty()) continue;
                String[] tokens = line.split(",", -1);
                try {
                    String id = token(tokens, idx, "id", "ID");
                    String name = token(tokens, idx, "name", "Name");
                    String email = token(tokens, idx, "email", "Email");
                    String interest = token(tokens, idx, "preferredgame", "preferred_game", "interest", "preferredgame");
                    String skillStr = token(tokens, idx, "skilllevel", "skill_level", "skill");
                    int skill = parseIntSafe(skillStr, 0);
                    String role = token(tokens, idx, "preferredrole", "preferred_role", "role");

                    Integer q1 = parseIntOrNull(token(tokens, idx, "q1", "q_1"));
                    Integer q2 = parseIntOrNull(token(tokens, idx, "q2", "q_2"));
                    Integer q3 = parseIntOrNull(token(tokens, idx, "q3", "q_3"));
                    Integer q4 = parseIntOrNull(token(tokens, idx, "q4", "q_4"));
                    Integer q5 = parseIntOrNull(token(tokens, idx, "q5", "q_5"));
                    Integer personalityScore = parseIntOrNull(token(tokens, idx, "personalityscore", "personality_score", "score"));

                    // Create a Participant object
                    Participant pObj = new Participant(id, name, email, interest, skill, role,
                            q1, q2, q3, q4, q5, personalityScore, personalityType);

                    // Validate personality score range (0-100)
                    int ps = pObj.getPersonalityScore();
                    if (ps < 0 || ps > 100) {
                        logger.warn("Personality score out of range at line " + ln + ": " + ps);
                    }
                    list.add(pObj);
                    successCount++;
                } catch (Exception ex) {
                    logger.warn("Skipping invalid line " + ln + ": " + ex.getMessage());
                }
            }
        }

        logger.info(String.format("CSV parsing completed - Lines: %d, Success: %d, Failed: %d",
                lineCount, successCount, lineCount - successCount));
        return list;
    }

    // Helper method to safely extract tokens from the CSV
    private String token(String[] tokens, Map<String,Integer> idx, String... keys) {
        for (String k : keys) {
            Integer i = idx.get(k.toLowerCase());
            if (i != null && i < tokens.length) return tokens[i].trim();
        }
        return "";
    }

    // Helper method to safely parse integers or return null
    private Integer parseIntOrNull(String s) {
        try {
            if (s == null || s.trim().isEmpty()) return null;
            return Integer.parseInt(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    // Helper method to parse integers safely with a default value
    private int parseIntSafe(String s, int def) {
        Integer v = parseIntOrNull(s);
        return v == null ? def : v;
    }

    public void writeTeamsToCSV(String path, List<Team> teams) throws IOException {
        logger.info("Writing " + teams.size() + " teams to CSV: " + path);

        Path p = Paths.get(path);
        try (BufferedWriter bw = Files.newBufferedWriter(p)) {
            bw.write("teamID,id,name,skillLevel,interest,role,personalityScore,personalityType\n");
            // Iterate through each team
            int participantCount = 0;
            for (Team t : teams) {
                // Iterate through each participant in the current team
                for (Participant participant : t.getMembers()) {  // Renamed variable 'p' to 'participant'
                    bw.write(participant.toCSVForTeam(t.getTeamID()) + "\n");
                    participantCount++;
                }
            }

            logger.debug(String.format("Team CSV written - Teams: %d, Participants: %d",
                    teams.size(), participantCount));
        } catch (IOException e) {
            logger.error("Failed to write teams to CSV: " + path, e);
            throw e;
        }
    }

    // Helper method to form teams (uses the Participant data)
    public List<Team> formTeams(List<Participant> participants, int teamSize) {
        logger.debug("Forming basic teams for " + participants.size() + " participants, team size: " + teamSize);

        List<Team> teams = new ArrayList<>();
        int teamID = 1; // Initialize team IDs

        // Create teams based on team size
        for (int i = 0; i < (participants.size() + teamSize - 1) / teamSize; i++) {
            teams.add(new Team(teamID++));  // Create a new team and assign an ID
        }

        // Assign participants to teams
        for (Participant p : participants) {
            Team bestTeam = null;
            for (Team t : teams) {
                if (t.size() < teamSize) {  // Check for available team slots
                    bestTeam = t;
                    break;
                }
            }

            // Add the participant to the best available team
            if (bestTeam != null) {
                bestTeam.addMember(p);
            }
        }

        logger.debug("Basic team formation completed - Teams: " + teams.size());
        return teams;  // Return the formed teams
    }
}