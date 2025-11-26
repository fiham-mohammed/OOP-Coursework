package teamate;

import java.io.*;
import java.util.Scanner;

public class SurveyManager {

    private final Scanner sc = new Scanner(System.in);

    private static int lastParticipantId = 100;

    // Method to conduct the interactive survey
    public Participant conductSurvey(String filePath) {
        Survey survey = new PersonalitySurvey();  // Can be changed to any other subclass like SkillSurvey, etc.
        return survey.conductSurvey(filePath);  // Polymorphic call
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

}
