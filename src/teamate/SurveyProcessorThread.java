package teamate;

import java.util.List;

/**
 * Thread for processing participant personality classification in parallel
 */
public class SurveyProcessorThread extends Thread {
    private List<Participant> participants;

    public SurveyProcessorThread(List<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public void run() {
        System.out.println("🔄 Processing personality classification for " + participants.size() + " participants...");

        for (Participant participant : participants) {
            // Ensure personality type is properly classified
            if (participant.getPersonalityType() == null || participant.getPersonalityType().isEmpty()) {
                String personalityType = participant.classifyPersonality(participant.getPersonalityScore());
                participant.setPersonalityType(personalityType);
            }

            // Validate and clamp personality score
            int score = participant.getPersonalityScore();
            if (score < 0) {
                participant.setPersonalityScore(0);
            } else if (score > 100) {
                participant.setPersonalityScore(100);
            }
        }

        System.out.println("✅ Personality classification completed.");
    }
}
