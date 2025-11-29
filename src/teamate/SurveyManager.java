package teamate;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public class SurveyManager {
    private final Logger logger = Logger.getInstance();
    private final Scanner sc = new Scanner(System.in);

    private static int lastParticipantId = 100;

    // Method to conduct the interactive survey
    public Participant conductSurvey(String filePath) {
        logger.debug("Starting survey conduction");
        Survey survey = new PersonalitySurvey();  // Can be changed to any other subclass like SkillSurvey, etc.
        return survey.conductSurvey(filePath);  // Polymorphic call
    }

    // Method to process the survey data in parallel for multiple participants using Thread
    public void processSurveyDataInParallel(String filePath, List<Participant> participants) {
        logger.info("Processing survey data in parallel for " + participants.size() + " participants");

        for (Participant participant : participants) {
            Thread thread = new Thread(new SurveyTask(participant, filePath));  // Create a new thread for each participant
            thread.start();  // Start the thread
        }
    }

    // A Runnable class for processing each survey
    static class SurveyTask implements Runnable {
        private final Participant participant;
        private final String filePath;
        private final Logger logger = Logger.getInstance();

        public SurveyTask(Participant participant, String filePath) {
            this.participant = participant;
            this.filePath = filePath;
        }

        @Override
        public void run() {
            try {
                // Process the participant's survey data here (e.g., save to file, calculate scores)
                System.out.println("Processing survey data for participant: " + participant.getName());
                logger.debug("Processing survey data for participant: " + participant.getId());

                // Example: Process data or save it to the CSV
                SurveyManager surveyManager = new SurveyManager();
                surveyManager.saveParticipantToCSV(participant, filePath);

                logger.debug("Completed processing for participant: " + participant.getId());
            } catch (Exception e) {
                logger.error("Error processing survey data for participant: " + participant.getId(), e);
            }
        }
    }

    // Save participant data (you may already have this method)
    public void saveParticipantToCSV(Participant participant, String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath, true))) {
            bw.write(participant.toCSVForParticipant());  // Assuming Participant class has toCSVForParticipant()
            bw.newLine();
            logger.debug("Participant saved to CSV: " + participant.getId());
        } catch (IOException e) {
            logger.error("Error saving participant to CSV: " + participant.getId(), e);
            System.err.println("Error saving participant to CSV: " + e.getMessage());
        }
    }
}