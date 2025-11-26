package teamate;

import java.io.*;
import java.util.Scanner;

public class PersonalitySurvey extends Survey {
    private final Scanner sc = new Scanner(System.in);

    @Override
    public Participant conductSurvey(String filePath) {
        System.out.println("\n=== New Personality Survey ===");

        // Collect participant details
        String id = generateNewParticipantId(filePath);

        System.out.println("Generated Participant ID: " + id);

        System.out.print("Enter Name: ");
        String name = sc.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = sc.nextLine().trim();

        // ---- Personality Questions (Rating 1 to 5) ----
        System.out.println("\nRate each question from 1 (Strongly Disagree) to 5 (Strongly Agree):");
        int q1 = askInt("Q1: I enjoy taking the lead and guiding others during group activities.");
        int q2 = askInt("Q2: I prefer analyzing situations and coming up with strategic solutions.");
        int q3 = askInt("Q3: I work well with others and enjoy collaborative teamwork.");
        int q4 = askInt("Q4: I am calm under pressure and can help maintain team morale.");
        int q5 = askInt("Q5: I like making quick decisions and adapting in dynamic situations.");

        // ---- Calculate total score and scale to 100 ----
        int totalScore = q1 + q2 + q3 + q4 + q5;
        int scaledScore = totalScore * 4; // Scaling to 100

        // ---- Personality Type Classification ----
        String personalityType = classifyPersonality(scaledScore);

        // ---- Interest Selection ----
        String interest = askInterest();

        // ---- Role Selection ----
        String role = askRole();

        // ---- Skill Level ----
        int skill = askInt("\nEnter Skill Level (1–10): ");

        // Create a new participant
        Participant newParticipant = new Participant(id, name, email, interest, skill, role, q1, q2, q3, q4, q5, scaledScore, personalityType);

        // Save new participant to the CSV file
        saveParticipantToCSV(newParticipant, filePath);

        return newParticipant;
    }


    // Helper method to ask integer questions
    private int askInt(String msg) {
        while (true) {
            try {
                System.out.print(msg + ": ");
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    // Helper method to ask for interest
    private String askInterest() {
        System.out.println("\nSelect your Interest:");
        System.out.println("1. Valorant");
        System.out.println("2. Dota");
        System.out.println("3. FIFA");
        System.out.println("4. Basketball");
        System.out.println("5. Badminton");

        return switch (askInt("Enter number: ")) {
            case 1 -> "Valorant";
            case 2 -> "Dota";
            case 3 -> "FIFA";
            case 4 -> "Basketball";
            case 5 -> "Badminton";
            default -> "Unknown";
        };
    }

    // Helper method to ask for role
    private String askRole() {
        System.out.println("\nSelect Preferred Role:");
        System.out.println("1. Defender");
        System.out.println("2. Strategist");
        System.out.println("3. Attacker");
        System.out.println("4. Supporter");
        System.out.println("5. Coordinator");

        return switch (askInt("Enter number: ")) {
            case 1 -> "Defender";
            case 2 -> "Strategist";
            case 3 -> "Attacker";
            case 4 -> "Supporter";
            case 5 -> "Coordinator";
            default -> "Unknown";
        };
    }

    // Personality classification based on score
    private String classifyPersonality(int score) {
        if (score >= 90) return "Leader";
        if (score >= 70) return "Balanced";
        if (score >= 50) return "Thinker";
        return "Undefined";
    }

    // Save the new participant to the CSV file
    private void saveParticipantToCSV(Participant newParticipant, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            // Write new participant data as CSV (without header)
            bw.write(newParticipant.toCSVForParticipant());  // Correct CSV format for participant
            bw.newLine(); // Move to the next line
        } catch (IOException e) {
            System.err.println("Error saving participant to CSV: " + e.getMessage());
        }
    }
    // Generate new Participant ID starting from the last ID in the CSV
    private String generateNewParticipantId(String filePath) {
        int lastParticipantId = getLastParticipantId(filePath);
        lastParticipantId++;  // Increment to get the next ID
        return "P" + lastParticipantId;
    }

    // Method to read the last participant ID from the CSV file
    private int getLastParticipantId(String filePath) {
        int lastId = 100;  // Default ID if no participants are in the file

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Assuming the participant ID is in the first column in each line
                String[] tokens = line.split(",", -1);
                String participantId = tokens[0].trim();

                // Check if the ID starts with "P" and contains only numbers after "P"
                if (participantId.startsWith("P") && participantId.length() > 1) {
                    try {
                        int numericId = Integer.parseInt(participantId.substring(1));  // Parse the numeric part after "P"
                        lastId = numericId;  // Update lastId with the numeric ID
                    } catch (NumberFormatException e) {
                        // Skip invalid IDs or log the error
                        System.out.println("Invalid ID format found: " + participantId);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lastId;
    }
}

