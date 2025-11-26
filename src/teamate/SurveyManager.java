package teamate;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class SurveyManager {

    private final Scanner sc = new Scanner(System.in);

    private static int lastParticipantId = 100;

    // Method to conduct the interactive survey
    public Participant conductSurvey(String filePath) {
        Survey survey = new PersonalitySurvey();  // Can be changed to any other subclass like SkillSurvey, etc.
        return survey.conductSurvey(filePath);  // Polymorphic call
    }
    // Method to process the survey data in parallel for multiple participants using Thread
    public void processSurveyDataInParallel(String filePath, List<Participant> participants) {
        for (Participant participant : participants) {
            Thread thread = new Thread(new SurveyTask(participant, filePath));  // Create a new thread for each participant
            thread.start();  // Start the thread
        }
    }

    // A Runnable class for processing each survey
    static class SurveyTask implements Runnable {
        private final Participant participant;
        private final String filePath;

        public SurveyTask(Participant participant, String filePath) {
            this.participant = participant;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            // Process the participant's survey data here (e.g., save to file, calculate scores)
            System.out.println("Processing survey data for participant: " + participant.getName());

            // Example: Process data or save it to the CSV
            SurveyManager surveyManager = new SurveyManager();
            surveyManager.saveParticipantToCSV(participant, filePath);
        }
    }

    // Save participant data (you may already have this method)
    public void saveParticipantToCSV(Participant participant, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(participant.toCSVForParticipant());  // Assuming Participant class has toCSVForParticipant()
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Error saving participant to CSV: " + e.getMessage());
        }
    }
}
