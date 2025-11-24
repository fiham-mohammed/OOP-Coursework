package teamate;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final ErrorHandler EH = new ErrorHandler();
    private static final String DEFAULT_INPUT = "C:/Users/User/Downloads/New folder/teamate_coursework_full/participants_sample.csv";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FileManager fm = new FileManager();
        PersonalityClassifier pc = new PersonalityClassifier();
        List<Participant> participants = Collections.synchronizedList(new ArrayList<>());
        SurveyManager surveyManager = new SurveyManager();  // Initialize SurveyManager
        List<Team> teams = new ArrayList<>();

        System.out.println("=== TeamMate (Coursework Full Version) ===");


        while (true) {
            // Display menu
            System.out.println("\nSelect an option:");
            System.out.println("1. Load participants from CSV");
            System.out.println("2. Add a new participant (Interactive Survey)");
            System.out.println("3. Form Teams");
            System.out.println("4. Save Teams to CSV");
            System.out.println("5. Exit");

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
            }
            else if (option == 2) {
                // Add a new participant through the interactive survey
                Participant p = surveyManager.conductSurvey();
                participants.add(p);
                System.out.println("[INFO] Participant added successfully.");
            }
            else if (option == 3) {
                // Prompt for desired team size
                System.out.println("Enter desired team size:");
                int teamSize = 0;
                try {
                    teamSize = Integer.parseInt(sc.nextLine().trim());
                    if (teamSize <= 0) {
                        System.out.println("Team size must be greater than 0. Using default size 4.");
                        teamSize = 4;  // Default team size
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input, using default team size of 4.");
                    teamSize = 4;  // Default team size
                }

                // Form teams with the user-defined team size
                TeamBuilder builder = new TeamBuilder(participants, teamSize);
                teams = builder.formTeams();  // <---- assign to the persistent teams variable
                teams.forEach(System.out::println);
            }
            else if (option == 4) {
                // Save the formed teams to CSV
                System.out.println("Enter output CSV filename to save teams (or press Enter to skip):");
                String out = sc.nextLine().trim();
                if (!out.isEmpty()) {
                    try {
                        if (teams == null || teams.isEmpty()) { // <--- check teams is not empty
                            EH.showError("No teams have been formed yet.");
                        } else {
                            fm.writeTeamsToCSV(out, teams); // <--- FIX: correct method call
                            EH.showInfo("Teams saved!");
                        }
                    } catch (IOException e) {
                        EH.showError("Failed to save teams: " + e.getMessage());
                    }
                }
            }
            else if (option == 5) {
                // Exit
                System.out.println("Exiting...");
                break;
            }
        }
    }

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
}
