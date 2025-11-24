package teamate;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * FileManager reads CSV files and writes formed teams.
 * Supports either Q1..Q5 columns OR a PersonalityScore column.
 */
public class FileManager {

    // Reads participants from the given CSV path
    public List<Participant> readParticipantsFromCSV(String path) throws IOException {
        Path p = Paths.get(path);
        if (!Files.exists(p)) throw new FileNotFoundException("CSV file not found: " + path);

        List<Participant> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(p)) {
            String header = br.readLine();
            if (header == null) return list;
            String[] cols = header.split(",", -1);
            Map<String, Integer> idx = new HashMap<>();
            for (int i = 0; i < cols.length; i++) idx.put(cols[i].trim().toLowerCase(), i);

            String line;
            int ln = 1;
            while ((line = br.readLine()) != null) {
                ln++;
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
                            q1, q2, q3, q4, q5, personalityScore);

                    // Validate personality score range (0-100)
                    int ps = pObj.getPersonalityScore();
                    if (ps < 0 || ps > 100) {
                        System.err.println("Warning: personality score out of range at line " + ln + ", clamped.");
                    }
                    list.add(pObj);
                } catch (Exception ex) {
                    System.err.println("Warning: skipping invalid line " + ln + " : " + ex.getMessage());
                }
            }
        }
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

    // Write the formed teams into a CSV
    public void writeTeamsToCSV(String path, List<Participant> teams) throws IOException {
        Path p = Paths.get(path);
        try (BufferedWriter bw = Files.newBufferedWriter(p)) {
            bw.write("teamID,id,name,skillLevel,interest,role,personalityScore,personalityType.\n");
            for (Participant t : teams) {
                bw.write(t.toString());
            }
        }
    }

    // Helper method to form teams (uses the Participant data)
    public List<Team> formTeams(List<Participant> participants, int teamSize) {
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

        return teams;  // Return the formed teams
    }
}
