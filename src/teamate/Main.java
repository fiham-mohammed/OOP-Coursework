package teamate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final ErrorHandler EH = new ErrorHandler();
    private static final String DEFAULT_INPUT = "C:/Users/User/Downloads/New folder/teamate_coursework_full/participants_sample.csv";
    private static List<Team> teams;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FileManager fm = new FileManager();
        PersonalityClassifier pc = new PersonalityClassifier();
        List<Participant> participants = Collections.synchronizedList(new ArrayList<>());
        SurveyManager surveyManager = new SurveyManager();  // Initialize SurveyManager

        System.out.println("=== TeamMate System ===");
        System.out.println("Are you a:");
        System.out.println("1. Organizer");
        System.out.println("2. Participant");

        System.out.print("\nEnter option: ");
        int userRole = Integer.parseInt(sc.nextLine().trim());

        if (userRole == 1) {
            // Organizer Flow
            handleOrganizerFlow(sc, fm, participants, pc);
        } else if (userRole == 2) {
            // Participant Flow (Start survey)
            handleParticipantSurvey(surveyManager, participants, sc);
        } else {
            System.out.println("Invalid option. Exiting.");
        }
    }

    // Handle the organizer menu
    private static void handleOrganizerFlow(Scanner sc, FileManager fm, List<Participant> participants, PersonalityClassifier pc) {
        while (true) {
            System.out.println("\n=== Organizer Menu ===");
            System.out.println("1. Load participants from CSV");
            System.out.println("2. View participants");
            System.out.println("3. Edit participant details");
            System.out.println("4. Form teams");
            System.out.println("5. Save teams to CSV");
            System.out.println("6. Exit");

            System.out.print("\nEnter option: ");
            int option = Integer.parseInt(sc.nextLine().trim());

            if (option == 1) {
                // Load participants from CSV (in parallel)
                System.out.println("Enter path to participants CSV (Enter = default):");
                String inputPath = sc.nextLine().trim();
                if (inputPath.isEmpty()) {
                    inputPath = DEFAULT_INPUT;
                    System.out.println("Using default: " + inputPath);
                }
                loadParticipantsFromCSV(fm, inputPath, participants, pc);
            } else if (option == 2) {
                // View participants
                viewParticipants(participants);
            } else if (option == 3) {
                // Edit participant details
                editParticipantDetails(sc, participants);
            } else if (option == 4) {
                // Form teams from participants concurrently
                System.out.println("Enter desired team size:");
                int teamSize = Integer.parseInt(sc.nextLine().trim());
                formTeamsAndDisplay(sc, participants, teamSize);
            } else if (option == 5) {
                // Save teams to CSV
                System.out.println("Enter output CSV filename to save teams:");
                String out = sc.nextLine().trim();
                if (!out.isEmpty()) {
                    saveTeamsToCSV(fm, out, participants);
                }
            } else if (option == 6) {
                // Exit the program
                System.out.println("Exiting...");
                break;
            }
        }
    }

    // Handle participant survey flow
    private static void handleParticipantSurvey(SurveyManager surveyManager, List<Participant> participants, Scanner sc) {
        System.out.println("Enter the CSV file path to save the new participant (e.g., participants_sample.csv):");
        String filePath = sc.nextLine().trim();

        // If no file path is entered, default to "participants_sample.csv"
        if (filePath.isEmpty()) {
            filePath = "C:/Users/User/Downloads/New folder/teamate_coursework_full/participants_sample.csv";
        }

        // Process survey concurrently for multiple participants (if needed)
        Participant newParticipant = surveyManager.conductSurvey(filePath);
        participants.add(newParticipant);
        System.out.println("[INFO] Participant added successfully.");
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
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("participants_sample.csv"))) {
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
        // Use a thread pool for concurrent processing
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        try {
            List<Participant> loaded = fm.readParticipantsFromCSV(inputPath);
            if (loaded.isEmpty()) {
                EH.showError("No participants loaded. Check CSV.");
                return;
            }

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

    // Form teams and display them concurrently
    private static void formTeamsAndDisplay(Scanner sc, List<Participant> participants, int teamSize) {
        // Use a thread pool for concurrent team formation
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        TeamBuilder builder = new TeamBuilder(participants, teamSize);

        // Submit the team formation task to the thread pool
        executorService.submit(() -> {
            List<Team> newTeams = builder.formTeams();
            teams = newTeams; // Assign the formed teams to the static variable
            System.out.println("\n=== Formed Teams ===");
            for (Team t : newTeams) {
                System.out.println(t);
            }
        });

        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Save teams to CSV
    private static void saveTeamsToCSV(FileManager fm, String out, List<Participant> participants) {
        try {
            fm.writeTeamsToCSV(out, teams);
            EH.showInfo("Teams saved!");
        } catch (IOException e) {
            EH.showError("Failed to save teams: " + e.getMessage());
        }
    }
}
