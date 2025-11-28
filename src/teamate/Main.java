package teamate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final ErrorHandler EH = new ErrorHandler();
    private static final String DEFAULT_INPUT = "C:/Users/User/Downloads/New folder/teamate_coursework_full/participants_sample.csv";
    private static List<Team> teams = new ArrayList<>();
    private static List<Participant> participants = Collections.synchronizedList(new ArrayList<>());

    // Enhanced data structures for better team management
    private static List<Team> wellBalancedTeams = new ArrayList<>();
    private static List<Team> secondaryTeams = new ArrayList<>();
    private static List<Participant> unassignedParticipants = new ArrayList<>();
    private static Integer teamSize = null;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FileManager fm = new FileManager();
        PersonalityClassifier pc = new PersonalityClassifier();
        SurveyManager surveyManager = new SurveyManager();

        System.out.println("=== TeamMate System ===");
        System.out.println("Are you a:");
        System.out.println("1. Organizer");
        System.out.println("2. Participant");

        int userRole = readIntInput(sc, "\nEnter option: ", 1, 2);
        if (userRole == 1) {
            // Organizer Flow - using EnhancedTeamBuilder for better matching
            TeamBuilder teamBuilder = new TeamBuilder(participants, 4); // default size
            handleOrganizerFlow(sc, fm, participants, pc, teamBuilder);
        } else if (userRole == 2) {
            // Participant Flow (Start survey)
            handleParticipantSurvey(surveyManager, participants, sc);
        } else {
            System.out.println("Invalid option. Exiting.");
        }
    }

    // Helper to safely read integer input within a range
    private static int readIntInput(Scanner sc, String prompt, int min, int max) {
        int result = -1;
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) {
                System.out.println("Input cannot be empty. Please enter a number.");
                continue;
            }
            try {
                result = Integer.parseInt(line);
                if (result < min || result > max) {
                    System.out.println("Invalid choice! Please enter a number between " + min + " and " + max + ".");
                } else {
                    break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a valid number between " + min + " and " + max + ".");
            }
        }
        return result;
    }

    // Enhanced Organizer Flow with better team formation
    private static void handleOrganizerFlow(Scanner sc, FileManager fm, List<Participant> participants,
                                            PersonalityClassifier pc, TeamBuilder teamBuilder) {
        while (true) {
            System.out.println("\n=== Organizer Menu ===");
            System.out.println("1. Load participants from CSV");
            System.out.println("2. View participants");
            System.out.println("3. Edit participant details");
            System.out.println("4. Set team size");
            System.out.println("5. Form balanced teams (Enhanced)");
            System.out.println("6. View all teams");
            System.out.println("7. View unassigned participants");
            System.out.println("8. Save teams to CSV");
            System.out.println("9. Exit");

            int option = readIntInput(sc, "\nEnter option: ", 1, 9);

            switch (option) {
                case 1:
                    // Load participants from CSV (in parallel)
                    System.out.println("Enter path to participants CSV (Enter = default):");
                    String inputPath = sc.nextLine().trim();
                    if (inputPath.isEmpty()) {
                        inputPath = DEFAULT_INPUT;
                        System.out.println("Using default: " + inputPath);
                    }
                    loadParticipantsFromCSV(fm, inputPath, participants, pc);
                    break;
                case 2:
                    // View participants
                    viewParticipants(participants);
                    break;
                case 3:
                    // Edit participant details
                    editParticipantDetails(sc, participants);
                    break;
                case 4:
                    // Set team size
                    setTeamSize(sc);
                    break;
                case 5:
                    // Form teams from participants with enhanced algorithm
                    if (teamSize == null) {
                        System.out.println("Please set team size first (Option 4).");
                        break;
                    }
                    formEnhancedTeams(participants, teamSize);
                    break;
                case 6:
                    // View all teams
                    viewAllTeams();
                    break;
                case 7:
                    // View unassigned participants
                    viewUnassignedParticipants();
                    break;
                case 8:
                    // Save teams to CSV
                    System.out.println("Enter output CSV filename to save teams:");
                    String out = sc.nextLine().trim();
                    if (!out.isEmpty()) {
                        saveAllTeamsToCSV(fm, out);
                    }
                    break;
                case 9:
                    // Exit the program
                    System.out.println("Exiting...");
                    return;
            }
        }
    }

    // Enhanced team formation with proper matching strategy
    private static void formEnhancedTeams(List<Participant> participants, int teamSize) {
        if (participants.isEmpty()) {
            System.out.println("No participants loaded. Please load participants first.");
            return;
        }

        try {
            System.out.println("🔄 Forming balanced teams with enhanced algorithm...");

            // Ensure all participants have proper personality classification
            for (Participant p : participants) {
                if (p.getPersonalityType() == null || p.getPersonalityType().isEmpty()) {
                    String personalityType = p.classifyPersonality(p.getPersonalityScore());
                    p.setPersonalityType(personalityType);
                }
            }

            // Use EnhancedTeamBuilder for better team formation
            TeamBuilder builder = new TeamBuilder(participants, teamSize);
            Map<String, Object> result = builder.formAllTeams();

            // Store results
            wellBalancedTeams = (List<Team>) result.get("wellBalanced");
            secondaryTeams = (List<Team>) result.get("secondary");
            unassignedParticipants = (List<Participant>) result.get("leftover");

            // Combine all teams for backward compatibility
            teams = new ArrayList<>();
            teams.addAll(wellBalancedTeams);
            teams.addAll(secondaryTeams);

            System.out.println("\n🎉 Enhanced Team Formation Completed!");
            System.out.println("Well-Balanced Teams: " + wellBalancedTeams.size());
            System.out.println("Secondary Teams: " + secondaryTeams.size());
            System.out.println("Unassigned Participants: " + unassignedParticipants.size());
            System.out.println("Total Teams Formed: " + teams.size());

        } catch (Exception e) {
            System.out.println("❌ Error during team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Set team size
    private static void setTeamSize(Scanner sc) {
        System.out.print("Enter team size: ");
        try {
            int size = Integer.parseInt(sc.nextLine().trim());
            if (size <= 1) {
                System.out.println("❌ Team size must be greater than 1.");
                return;
            }
            teamSize = size;
            System.out.println("✅ Team size set to: " + size);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid number format.");
        }
    }

    // View all teams (enhanced)
    private static void viewAllTeams() {
        if (teams.isEmpty()) {
            System.out.println("No teams formed yet. Run team formation first.");
            return;
        }

        System.out.println("\n=== ALL FORMED TEAMS ===");

        if (!wellBalancedTeams.isEmpty()) {
            System.out.println("\n--- WELL-BALANCED TEAMS ---");
            for (int i = 0; i < wellBalancedTeams.size(); i++) {
                System.out.println("WB-Team " + (i + 1) + ":");
                for (Participant p : wellBalancedTeams.get(i).getMembers()) {
                    System.out.println("  - " + p);
                }
                System.out.println();
            }
        }

        if (!secondaryTeams.isEmpty()) {
            System.out.println("\n--- SECONDARY TEAMS ---");
            for (int i = 0; i < secondaryTeams.size(); i++) {
                System.out.println("SC-Team " + (i + 1) + ":");
                for (Participant p : secondaryTeams.get(i).getMembers()) {
                    System.out.println("  - " + p);
                }
                System.out.println();
            }
        }

        System.out.println("Total Teams: " + teams.size());
    }

    // Save all teams to CSV (enhanced)
    private static void saveAllTeamsToCSV(FileManager fm, String filename) {
        try {
            fm.writeTeamsToCSV(filename, teams);
            EH.showInfo("All teams saved to: " + filename);

            // Also show unassigned count
            if (!unassignedParticipants.isEmpty()) {
                System.out.println("Note: " + unassignedParticipants.size() + " participants were not assigned to teams.");
            }
        } catch (IOException e) {
            EH.showError("Failed to save teams: " + e.getMessage());
        }
    }

    // ========== KEEP YOUR EXISTING METHODS BELOW ==========

    // Handle participant survey flow
    private static void handleParticipantSurvey(SurveyManager surveyManager, List<Participant> participants, Scanner sc) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            Participant newParticipant = surveyManager.conductSurvey(DEFAULT_INPUT);
            synchronized(participants) {
                participants.add(newParticipant);
            }
            System.out.println("[INFO] Participant added successfully (processed in parallel)");
        });
        executor.shutdown();
    }

    // Method to edit participant details
    private static void editParticipantDetails(Scanner sc, List<Participant> participants) {
        // Validate Participant ID
        Participant participantToEdit = null;
        while (participantToEdit == null) {
            System.out.print("Enter the participant's ID to edit: ");
            String participantId = sc.nextLine().trim();

            // Search for the participant by ID
            for (Participant participant : participants) {
                if (participant.getId().equals(participantId)) {
                    participantToEdit = participant;
                    break;
                }
            }

            if (participantToEdit == null) {
                System.out.println("Invalid ID! Please enter a valid participant ID.");
            }
        }

        // Display current details of the participant
        System.out.println("\nCurrent details of participant " + participantToEdit.getName() + ":");
        System.out.println("Name: " + participantToEdit.getName());
        System.out.println("Email: " + participantToEdit.getEmail());
        System.out.println("Interest: " + participantToEdit.getInterest());
        System.out.println("Skill Level: " + participantToEdit.getSkillLevel());
        System.out.println("Personality Type: " + participantToEdit.getPersonalityType());

        // Enter new name
        System.out.print("Enter new name (leave empty to keep current): ");
        String newName = sc.nextLine().trim();
        if (!newName.isEmpty()) {
            participantToEdit.setName(newName);
        }

        // Enter new email
        System.out.print("Enter new email (leave empty to keep current): ");
        String newEmail = sc.nextLine().trim();
        if (!newEmail.isEmpty()) {
            participantToEdit.setEmail(newEmail);
        }

        // Select new interest with validation
        String newInterest = selectInterest(sc);
        if (!newInterest.isEmpty()) {
            participantToEdit.setInterest(newInterest);
        }

        // Select new role with validation
        String newRole = selectRole(sc);
        if (!newRole.isEmpty()) {
            participantToEdit.setRole(newRole);
        }

        // Enter new skill level with validation
        int newSkillLevel = selectSkillLevel(sc);
        if (newSkillLevel != -1) {
            participantToEdit.setSkillLevel(newSkillLevel);
        }

        // Recalculate personality type based on updated values
        int totalScore = participantToEdit.computeTotalFromQuestions();
        String personalityType = participantToEdit.classifyPersonality(totalScore);
        participantToEdit.setPersonalityType(personalityType);

        // Save updated participant data back to CSV
        saveParticipantsToCSV(participants);
        System.out.println("Participant details updated successfully.");
    }

    private static String selectInterest(Scanner sc) {
        int interestChoice = -1;
        while (interestChoice < 1 || interestChoice > 5) {
            System.out.println("\nSelect your Interest:");
            System.out.println("1. Valorant");
            System.out.println("2. Dota");
            System.out.println("3. FIFA");
            System.out.println("4. Basketball");
            System.out.println("5. Badminton");

            System.out.print("Enter number: ");
            try {
                interestChoice = Integer.parseInt(sc.nextLine().trim());
                if (interestChoice < 1 || interestChoice > 5) {
                    System.out.println("Invalid choice! Please choose a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1 and 5.");
            }
        }

        switch (interestChoice) {
            case 1: return "Valorant";
            case 2: return "Dota";
            case 3: return "FIFA";
            case 4: return "Basketball";
            case 5: return "Badminton";
            default: return "Unknown";
        }
    }

    private static String selectRole(Scanner sc) {
        int roleChoice = -1;
        while (roleChoice < 1 || roleChoice > 5) {
            System.out.println("\nSelect Preferred Role:");
            System.out.println("1. Defender");
            System.out.println("2. Strategist");
            System.out.println("3. Attacker");
            System.out.println("4. Supporter");
            System.out.println("5. Coordinator");

            System.out.print("Enter number: ");
            try {
                roleChoice = Integer.parseInt(sc.nextLine().trim());
                if (roleChoice < 1 || roleChoice > 5) {
                    System.out.println("Invalid choice! Please choose a number between 1 and 5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1 and 5.");
            }
        }

        switch (roleChoice) {
            case 1: return "Defender";
            case 2: return "Strategist";
            case 3: return "Attacker";
            case 4: return "Supporter";
            case 5: return "Coordinator";
            default: return "Unknown";
        }
    }

    private static int selectSkillLevel(Scanner sc) {
        int skillLevel = -1;
        while (skillLevel < 1 || skillLevel > 10) {
            System.out.print("Enter new skill level (1-10, leave empty to keep current): ");
            String input = sc.nextLine().trim();

            if (input.isEmpty()) {
                return -1;  // If left empty, keep current skill level
            }

            try {
                skillLevel = Integer.parseInt(input);
                if (skillLevel < 1 || skillLevel > 10) {
                    System.out.println("Invalid skill level! Must be between 1 and 10.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number between 1 and 10.");
            }
        }
        return skillLevel;
    }

    private static void saveParticipantsToCSV(List<Participant> participants) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("participants_sample1.csv"))) {
            for (Participant participant : participants) {
                bw.write(participant.toCSVForParticipant());
                bw.newLine();
            }
            System.out.println("Participants saved to CSV.");
        } catch (IOException e) {
            System.err.println("Error saving participants to CSV: " + e.getMessage());
        }
    }

    // Load participants from CSV (with concurrency for parallel loading)
    private static void loadParticipantsFromCSV(FileManager fm, String inputPath, List<Participant> participants, PersonalityClassifier pc) {
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            List<Participant> loaded = fm.readParticipantsFromCSV(inputPath);
            if (loaded.isEmpty()) {
                EH.showError("No participants loaded. Check CSV.");
                return;
            }

            // Clear existing participants
            participants.clear();

            // Submit tasks for each participant to process them concurrently
            for (Participant p : loaded) {
                executorService.submit(new LoadParticipantTask(p, participants, pc));
            }

            executorService.shutdown();
            executorService.awaitTermination(60, TimeUnit.SECONDS);
            EH.showInfo("Loaded and classified " + participants.size() + " participants.");
        } catch (IOException | InterruptedException e) {
            EH.showError("Failed to load CSV: " + e.getMessage());
        }
    }

    // Runnable task to process each participant
    static class LoadParticipantTask implements Runnable {
        private final Participant participant;
        private final List<Participant> participants;
        private final PersonalityClassifier pc;

        public LoadParticipantTask(Participant participant, List<Participant> participants, PersonalityClassifier pc) {
            this.participant = participant;
            this.participants = participants;
            this.pc = pc;
        }

        @Override
        public void run() {
            int score = participant.getPersonalityScore();
            if (score < 0) score = 0;
            if (score > 100) score = 100;
            participant.setPersonalityType(pc.classify(score));
            synchronized (participants) {
                participants.add(participant);
            }
        }
    }

    // View all participants
    private static void viewParticipants(List<Participant> participants) {
        System.out.println("\n=== Participants ===");
        for (Participant p : participants) {
            System.out.println(p.toString());
        }
    }

    // View unassigned participants
    private static void viewUnassignedParticipants() {
        if (unassignedParticipants.isEmpty()) {
            System.out.println("[INFO] No unassigned participants.");
        } else {
            System.out.println("\n=== Unassigned Participants ===");
            for (Participant p : unassignedParticipants) {
                System.out.println(p.toString());
            }
        }
    }
}