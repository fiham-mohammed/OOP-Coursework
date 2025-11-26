package teamate;

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
                // Edit participant details (You can add edit functionality here later)
                System.out.println("Edit functionality is not implemented yet.");
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
