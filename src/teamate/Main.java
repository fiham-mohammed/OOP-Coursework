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
            handleParticipantSurvey(surveyManager, participants);
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
                // Load participants from CSV
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
                // Form teams from participants
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
    private static void handleParticipantSurvey(SurveyManager surveyManager, List<Participant> participants) {
        Participant newParticipant = surveyManager.conductSurvey();
        participants.add(newParticipant);
        System.out.println("[INFO] Participant added successfully.");
    }

    // Load participants from CSV
    private static void loadParticipantsFromCSV(FileManager fm, String inputPath, List<Participant> participants, PersonalityClassifier pc) {
        try {
            List<Participant> loaded = fm.readParticipantsFromCSV(inputPath);
            if (loaded.isEmpty()) {
                EH.showError("No participants loaded. Check CSV.");
                return;
            }
            for (Participant p : loaded) {
                int score = p.getPersonalityScore();
                if (score < 0) score = 0;
                if (score > 100) score = 100;
                p.setPersonalityType(pc.classify(score));
                participants.add(p);
            }
            EH.showInfo("Loaded and classified " + participants.size() + " participants.");
        } catch (IOException e) {
            EH.showError("Failed to load CSV: " + e.getMessage());
        }
    }

    // View all participants
    private static void viewParticipants(List<Participant> participants) {
        System.out.println("\n=== Participants ===");
        for (Participant p : participants) {
            System.out.println(p.toString());
        }
    }

    // Form teams and display them
    private static void formTeamsAndDisplay(Scanner sc, List<Participant> participants, int teamSize) {
        TeamBuilder builder = new TeamBuilder(participants, teamSize);
        List<Team> newTeams = builder.formTeams();
        teams = newTeams; // Assign the formed teams to the static variable
        System.out.println("\n=== Formed Teams ===");
        for (Team t : newTeams) {
            System.out.println(t);
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
