package teamate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final ErrorHandler EH = new ErrorHandler();
    private static final Logger logger = Logger.getInstance();
    private static final String DEFAULT_INPUT = "C:/Users/User/Downloads/New folder/teamate_coursework_full/participants_sample.csv";
    private static List<Team> teams = new ArrayList<>();
    private static List<Participant> participants = Collections.synchronizedList(new ArrayList<>());

    // Enhanced data structures for better team management
    private static List<Team> wellBalancedTeams = new ArrayList<>();
    private static List<Team> secondaryTeams = new ArrayList<>();
    private static List<Participant> unassignedParticipants = new ArrayList<>();
    private static Integer teamSize = null;

    public static void main(String[] args) {
        logger.info("TeamMate System starting...");

        try {
            Scanner sc = new Scanner(System.in);
            FileManager fm = new FileManager();
            PersonalityClassifier pc = new PersonalityClassifier();
            SurveyManager surveyManager = new SurveyManager();

            logger.debug("Initialized core components");

            System.out.println("=== TeamMate System ===");
            System.out.println("Are you a:");
            System.out.println("1. Organizer");
            System.out.println("2. Participant");
            System.out.println("3. Exit");

            int userRole = readIntInput(sc, "\nEnter option: ", 1, 3);
            if (userRole == 1) {
                logger.info("User selected role: Organizer");
            } else if (userRole == 2) {
                logger.info("User selected role: Participant");
            } else if (userRole == 3) {
                logger.info("User selected role: Exit");
            }


            if (userRole == 1) {
                // Organizer Flow - using EnhancedTeamBuilder for better matching
                TeamBuilder teamBuilder = new TeamBuilder(participants, 4); // default size
                handleOrganizerFlow(sc, fm, participants, pc, teamBuilder);
            } else if (userRole == 2) {
                // Participant Flow (Start survey)
                handleParticipantSurvey(surveyManager, participants, sc);
            } else if (userRole == 3) {
                System.out.println("Exiting TeamMate System");
                return;
            } else {
                logger.warn("Invalid user role selected: " + userRole);
                System.out.println("Invalid option. Exiting.");
            }

            logger.info("TeamMate System shutting down normally");
        } catch (Exception e) {
            logger.error("Fatal error in main method", e);
            EH.showError("System encountered a fatal error: " + e.getMessage());
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
        logger.info("Organizer flow started");

        while (true) {
            System.out.println("\n=== Organizer Menu ===");
            System.out.println("1. Load participants from CSV");
            System.out.println("2. View participants");
            System.out.println("3. Edit participant details");
            System.out.println("4. Form teams");  // Merged option: Set team size + Form balanced teams
            System.out.println("5. View all teams");
            System.out.println("6. View unassigned participants");
            System.out.println("7. Save teams to CSV");
            System.out.println("8. Exit");

            int option = readIntInput(sc, "\nEnter option: ", 1, 8);  // Updated to 8 options

            switch (option) {
                case 1:
                    logger.debug("Organizer selected: Load participants from CSV");
                    System.out.println("Enter path to participants CSV (Enter = default):");
                    String inputPath = sc.nextLine().trim();
                    if (inputPath.isEmpty()) {
                        inputPath = DEFAULT_INPUT;
                        logger.info("Using default CSV path: " + inputPath);
                    } else {
                        logger.info("Using custom CSV path: " + inputPath);
                    }
                    loadParticipantsFromCSV(fm, inputPath, participants, pc);
                    break;
                case 2:
                    logger.debug("Organizer selected: View participants");
                    viewParticipants(participants);
                    break;
                case 3:
                    logger.debug("Organizer selected: Edit participant details");
                    editParticipantDetails(sc, participants);
                    break;
                case 4:  // Merged: Form teams (includes team size setting)
                    logger.debug("Organizer selected: Form teams");
                    handleFormTeams(sc, participants);
                    break;
                case 5:
                    logger.debug("Organizer selected: View all teams");
                    viewAllTeams();
                    break;
                case 6:
                    logger.debug("Organizer selected: View unassigned participants");
                    viewUnassignedParticipants();
                    break;
                case 7:
                    logger.debug("Organizer selected: Save teams to CSV");
                    handleSaveTeamsToCSV(sc, fm);
                    break;
                case 8:
                    logger.info("Organizer exiting application");
                    System.out.println("Exiting...");
                    return;
            }
        }
    }

    // New method to handle the combined team formation process
    private static void handleFormTeams(Scanner sc, List<Participant> participants) {
        logger.info("Starting combined team formation process");

        // Check if participants are loaded
        if (participants.isEmpty()) {
            logger.warn("Team formation attempted with empty participant list");
            System.out.println("No participants loaded. Please load participants first (Option 1).");
            return;
        }

        // Step 1: Ask for team size
        System.out.println("\n=== Team Formation ===");
        System.out.print("Enter team size (must be greater than 3): ");

        int size = -1;
        while (size <= 3) {
            try {
                String input = sc.nextLine().trim();
                size = Integer.parseInt(input);

                if (size <= 3) {
                    System.out.println("❌ Team size must be greater than 3.");
                    System.out.print("Please enter a valid team size: ");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input! Please enter a valid number.");
                System.out.print("Enter team size (must be greater than 3): ");
            }
        }

        teamSize = size; // Set the global teamSize variable
        logger.info("Team size set to: " + size);
        System.out.println("✅ Team size set to: " + size);

        // Step 2: Form teams with the specified size
        logger.debug("Starting team formation with size: " + teamSize);
        formEnhancedTeams(participants, teamSize);
    }

    // Enhanced team formation with proper matching strategy
    private static void formEnhancedTeams(List<Participant> participants, int teamSize) {
        logger.debug("Starting enhanced team formation process");

        if (participants.isEmpty()) {
            logger.warn("Team formation attempted with empty participant list");
            System.out.println("No participants loaded. Please load participants first.");
            return;
        }

        try {
            logger.info("Forming balanced teams for " + participants.size() + " participants");

            // Log participant statistics
            long validParticipants = participants.stream().filter(Participant::isValid).count();
            long eligibleParticipants = participants.stream().filter(Participant::isEligibleForTeams).count();
            logger.debug(String.format("Participant stats - Total: %d, Valid: %d, Eligible: %d",
                    participants.size(), validParticipants, eligibleParticipants));

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

            logger.info(String.format(
                    "Team formation completed - Well-balanced: %d, Secondary: %d, Unassigned: %d",
                    wellBalancedTeams.size(), secondaryTeams.size(), unassignedParticipants.size()
            ));

            System.out.println("\n🎉 Enhanced Team Formation Completed!");
            System.out.println("Well-Balanced Teams: " + wellBalancedTeams.size());
            System.out.println("Secondary Teams: " + secondaryTeams.size());
            System.out.println("Unassigned Participants: " + unassignedParticipants.size());
            System.out.println("Total Teams Formed: " + teams.size());

        } catch (Exception e) {
            logger.error("Team formation failed", e);
            System.out.println("❌ Error during team formation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Set team size (kept for backward compatibility if needed elsewhere)
    private static void setTeamSize(Scanner sc) {
        System.out.print("Enter team size: ");
        try {
            int size = Integer.parseInt(sc.nextLine().trim());
            if (size <= 3) {
                logger.warn("Invalid team size attempted: " + size);
                System.out.println("❌ Team size must be greater than 3.");
                return;
            }
            teamSize = size;
            logger.info("Team size set to: " + size);
            System.out.println("✅ Team size set to: " + size);
        } catch (NumberFormatException e) {
            logger.error("Invalid team size input format", e);
            System.out.println("❌ Invalid number format.");
        }
    }

    // View all teams (enhanced)
    private static void viewAllTeams() {
        if (teams.isEmpty()) {
            logger.debug("View teams requested but no teams formed");
            System.out.println("❌ No teams formed yet. Run team formation first (Option 4).");
            return;
        }

        logger.debug("Displaying all formed teams");
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
        // Double-check that teams exist before saving
        if (teams.isEmpty()) {
            logger.error("Attempted to save empty teams list to CSV: " + filename);
            EH.showError("No teams to save. Please form teams first.");
            return;
        }

        try {
            logger.info("Saving teams to CSV: " + filename);
            fm.writeTeamsToCSV(filename, teams);
            EH.showInfo("All teams saved to: " + filename);

            // Also show unassigned count
            if (!unassignedParticipants.isEmpty()) {
                logger.debug("Unassigned participants count: " + unassignedParticipants.size());
                System.out.println("Note: " + unassignedParticipants.size() + " participants were not assigned to teams.");
            }
        } catch (IOException e) {
            logger.error("Failed to save teams to CSV: " + filename, e);
            EH.showError("Failed to save teams: " + e.getMessage());
        }
    }

    // ========== EXISTING METHODS WITH LOGGING ==========

    // Handle participant survey flow
    private static void handleParticipantSurvey(SurveyManager surveyManager, List<Participant> participants, Scanner sc) {
        logger.info("Starting participant survey flow");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                Participant newParticipant = surveyManager.conductSurvey(DEFAULT_INPUT);
                synchronized(participants) {
                    participants.add(newParticipant);
                }
                logger.info("New participant added successfully: " + newParticipant.getId());
                System.out.println("[INFO] Participant added successfully (processed in parallel)");
            } catch (Exception e) {
                logger.error("Error during participant survey", e);
            }
        });
        executor.shutdown();
    }

    // Method to edit participant details
    private static void editParticipantDetails(Scanner sc, List<Participant> participants) {
        logger.debug("Starting participant edit process");

        // Check if participants are loaded
        if (participants.isEmpty()) {
            logger.warn("Team formation attempted with empty participant list");
            System.out.println("No participants loaded. Please load participants first (Option 1).");
            return;
        }

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
                logger.warn("Invalid participant ID entered: " + participantId);
                System.out.println("Invalid ID! Please enter a valid participant ID.");
            }
        }

        logger.debug("Editing participant: " + participantToEdit.getId());

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
            logger.debug("Updated name for participant: " + participantToEdit.getId());
        }

        // Enter new email
        System.out.print("Enter new email (leave empty to keep current): ");
        String newEmail = sc.nextLine().trim();
        if (!newEmail.isEmpty()) {
            participantToEdit.setEmail(newEmail);
            logger.debug("Updated email for participant: " + participantToEdit.getId());
        }

        // Select new interest with validation
        String newInterest = selectInterest(sc);
        if (!newInterest.isEmpty()) {
            participantToEdit.setInterest(newInterest);
            logger.debug("Updated interest for participant: " + participantToEdit.getId());
        }

        // Select new role with validation
        String newRole = selectRole(sc);
        if (!newRole.isEmpty()) {
            participantToEdit.setRole(newRole);
            logger.debug("Updated role for participant: " + participantToEdit.getId());
        }

        // Enter new skill level with validation
        int newSkillLevel = selectSkillLevel(sc);
        if (newSkillLevel != -1) {
            participantToEdit.setSkillLevel(newSkillLevel);
            logger.debug("Updated skill level for participant: " + participantToEdit.getId());
        }

        // Recalculate personality type based on updated values
        int totalScore = participantToEdit.computeTotalFromQuestions();
        String personalityType = participantToEdit.classifyPersonality(totalScore);
        participantToEdit.setPersonalityType(personalityType);

        // Save updated participant data back to CSV
        saveParticipantsToCSV(participants);
        logger.info("Participant details updated successfully: " + participantToEdit.getId());
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
            logger.info("Participants saved to CSV, count: " + participants.size());
            System.out.println("Participants saved to CSV.");
        } catch (IOException e) {
            logger.error("Error saving participants to CSV", e);
            System.err.println("Error saving participants to CSV: " + e.getMessage());
        }
    }

    // Load participants from CSV (with concurrency for parallel loading)
    private static void loadParticipantsFromCSV(FileManager fm, String inputPath, List<Participant> participants, PersonalityClassifier pc) {
        logger.info("Loading participants from CSV: " + inputPath);
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            List<Participant> loaded = fm.readParticipantsFromCSV(inputPath);
            if (loaded.isEmpty()) {
                logger.warn("CSV file loaded but no participants found: " + inputPath);
                EH.showError("No participants loaded. Check CSV.");
                return;
            }

            logger.debug("Raw CSV load completed, found " + loaded.size() + " entries");
            participants.clear();

            // STEP 1: Basic processing with threads
            for (Participant p : loaded) {
                executorService.submit(() -> {
                    try {
                        int score = p.getPersonalityScore();
                        if (score < 0) score = 0;
                        if (score > 100) score = 100;
                        p.setPersonalityType(pc.classify(score));
                        logger.debug("Basic processing: " + p.getId());
                    } catch (Exception e) {
                        logger.error("Error in basic processing: " + p.getId(), e);
                    }
                });
            }

            executorService.shutdown();
            executorService.awaitTermination(30, TimeUnit.SECONDS);

            // STEP 2: Advanced processing with SurveyProcessorThread
            logger.info("Starting advanced personality classification...");
            SurveyProcessorThread processor = new SurveyProcessorThread(loaded);
            processor.start();
            processor.join();

            // Add all to main list
            synchronized(participants) {
                participants.addAll(loaded);
            }

            logger.info("Successfully loaded " + loaded.size() + " participants");
            EH.showInfo("Loaded and classified " + loaded.size() + " participants.");

        } catch (IOException | InterruptedException e) {
            logger.error("Error loading CSV", e);
            EH.showError("Failed to load CSV: " + e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    // Runnable task to process each participant
    static class LoadParticipantTask implements Runnable {
        private final Participant participant;
        private final List<Participant> participants;
        private final PersonalityClassifier pc;
        private final Logger logger = Logger.getInstance();

        public LoadParticipantTask(Participant participant, List<Participant> participants, PersonalityClassifier pc) {
            this.participant = participant;
            this.participants = participants;
            this.pc = pc;
        }

        @Override
        public void run() {
            try {
                int score = participant.getPersonalityScore();
                if (score < 0) score = 0;
                if (score > 100) score = 100;
                participant.setPersonalityType(pc.classify(score));
                synchronized (participants) {
                    participants.add(participant);
                }
                logger.debug("Processed participant: " + participant.getId());
            } catch (Exception e) {
                logger.error("Error processing participant: " + participant.getId(), e);
            }
        }
    }

    // View all participants
    private static void viewParticipants(List<Participant> participants) {
        logger.debug("Viewing all participants, count: " + participants.size());

        // Check if participants are loaded
        if (participants.isEmpty()) {
            logger.warn("Team formation attempted with empty participant list");
            System.out.println("No participants loaded. Please load participants first (Option 1).");
            return;
        }

        System.out.println("\n=== Participants ===");
        for (Participant p : participants) {
            System.out.println(p.toString());
        }
    }

    // View unassigned participants
    private static void viewUnassignedParticipants() {
        if (unassignedParticipants.isEmpty()) {
            logger.debug("No unassigned participants to display");
            System.out.println("[INFO] No unassigned participants.");
        } else {
            logger.debug("Viewing unassigned participants, count: " + unassignedParticipants.size());
            System.out.println("\n=== Unassigned Participants ===");
            for (Participant p : unassignedParticipants) {
                System.out.println(p.toString());
            }
        }
    }

    // New method to handle saving teams with validation
    private static void handleSaveTeamsToCSV(Scanner sc, FileManager fm) {
        // Check if teams have been formed
        if (teams.isEmpty()) {
            logger.warn("Save teams attempted before any teams were formed");
            System.out.println("❌ No teams to save. Please form teams first (Option 4).");
            return;
        }

        // Only ask for filename if teams exist
        System.out.println("Enter output CSV filename to save teams:");
        String out = sc.nextLine().trim();
        if (!out.isEmpty()) {
            saveAllTeamsToCSV(fm, out);
        } else {
            logger.debug("Save teams cancelled - empty filename");
            System.out.println("Save cancelled.");
        }
    }
}