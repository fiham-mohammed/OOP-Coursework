package teamate;

import java.util.List;

/**
 * Thread for processing participant personality classification in parallel
 */
public class SurveyProcessorThread extends Thread {
    private final Logger logger = Logger.getInstance();
    private List<Participant> participants;

    public SurveyProcessorThread(List<Participant> participants) {
        this.participants = participants;
    }

    @Override
    public void run() {
        logger.info("🔄 Processing personality classification for " + participants.size() + " participants...");
        System.out.println("🔄 Processing personality classification for " + participants.size() + " participants...");

        int processedCount = 0;
        for (Participant participant : participants) {
            try {
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

                processedCount++;
                logger.debug("Processed participant: " + participant.getId());
            } catch (Exception e) {
                logger.error("Error processing participant: " + participant.getId(), e);
            }
        }

        logger.info("✅ Personality classification completed for " + processedCount + " participants.");
        System.out.println("✅ Personality classification completed.");
    }
}