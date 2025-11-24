package teamate;

import java.util.Scanner;

public class SurveyManager {

    private final Scanner sc = new Scanner(System.in);

    // Method to conduct the interactive survey
    public Participant conductSurvey() {
        System.out.println("\n=== New Participant Survey ===");

        // Collect participant details
        System.out.print("Enter Participant ID: ");
        String id = sc.nextLine().trim();

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
        System.out.println("\nSelect your Interest:");
        System.out.println("1. Valorant");
        System.out.println("2. Dota");
        System.out.println("3. FIFA");
        System.out.println("4. Basketball");
        System.out.println("5. Badminton");

        String interest = switch (askInt("Enter number: ")) {
            case 1 -> "Valorant";
            case 2 -> "Dota";
            case 3 -> "FIFA";
            case 4 -> "Basketball";
            case 5 -> "Badminton";
            default -> "Unknown";
        };

        // ---- Role Selection ----
        System.out.println("\nSelect Preferred Role:");
        System.out.println("1. Defender");
        System.out.println("2. Strategist");
        System.out.println("3. Attacker");
        System.out.println("4. Supporter");
        System.out.println("5. Coordinator");

        String role = switch (askInt("Enter number: ")) {
            case 1 -> "Defender";
            case 2 -> "Strategist";
            case 3 -> "Attacker";
            case 4 -> "Supporter";
            case 5 -> "Coordinator";
            default -> "Unknown";
        };

        // ---- Skill Level ----
        int skill = askInt("\nEnter Skill Level (1–10): ");

        // Create participant object with personality type
        return new Participant(id, name, email, interest, skill, role, q1, q2, q3, q4, q5, scaledScore);
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

    // Helper method to classify personality type based on the scaled score
    private String classifyPersonality(int score) {
        if (score >= 90) return "Leader";
        if (score >= 70) return "Balanced";
        if (score >= 50) return "Thinker";
        return "Undefined";
    }
}
