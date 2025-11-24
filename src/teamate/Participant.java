package teamate;

/**
 * Participant data model.
 * Supports Q1..Q5 optional fields OR a direct PersonalityScore field.
 */
public class Participant {
    private String id;
    private String name;
    private String email;
    private String interest; // Preferred game
    private int skillLevel;
    private String role;
    // Optional five-question scores (1-5 expected, total score range: 5-25)
    private Integer q1, q2, q3, q4, q5;
    private int personalityScore; // Scaled 0-100
    private String personalityType;

    // Constructor to initialize participant data
    public Participant(String id, String name, String email,
                       String interest, int skillLevel, String role,
                       Integer q1, Integer q2, Integer q3, Integer q4, Integer q5,
                       Integer personalityScore) {
        this.id = safe(id);
        this.name = safe(name);
        this.email = safe(email);
        this.interest = safe(interest).isEmpty() ? "Unknown" : interest;
        this.skillLevel = Math.max(0, skillLevel);
        this.role = safe(role).isEmpty() ? "Unknown" : role;
        this.q1 = q1; this.q2 = q2; this.q3 = q3; this.q4 = q4; this.q5 = q5;

        // If 5 questions are provided, calculate the total score
        if (hasFiveQuestions()) {
            this.personalityScore = computeTotalFromQuestions() * 4; // Scale to 100
        } else if (personalityScore != null) {
            this.personalityScore = clamp(personalityScore, 0, 100);
        } else {
            this.personalityScore = 0;
        }

        // Classify personality type based on score
        this.personalityType = classifyPersonality(personalityScore);
    }

    // Helper method to ensure a non-null and trimmed string
    private String safe(String s) { return s == null ? "" : s.trim(); }

    // Check if all 5 personality questions are provided
    public boolean hasFiveQuestions() {
        return q1 != null && q2 != null && q3 != null && q4 != null && q5 != null;
    }

    // Calculate the total score from the 5 questions (range: 5-25)
    private int computeTotalFromQuestions() {
        int sum = 0;
        sum += (q1 != null ? q1 : 0);
        sum += (q2 != null ? q2 : 0);
        sum += (q3 != null ? q3 : 0);
        sum += (q4 != null ? q4 : 0);
        sum += (q5 != null ? q5 : 0);
        return sum;
    }

    // Clamp the score to a defined range (0-100)
    private int clamp(int v, int a, int b) {
        if (v < a) return a;
        if (v > b) return b;
        return v;
    }

    // Classify the personality based on the score
    private String classifyPersonality(int score) {
        if (score >= 90) return "Leader";
        if (score >= 70) return "Balanced";
        if (score >= 50) return "Thinker";
        return "Undefined"; // Default to Undefined if score < 50
    }

    // Getters and setters for the Participant class
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getInterest() { return interest; }
    public int getSkillLevel() { return skillLevel; }
    public String getRole() { return role; }
    public int getPersonalityScore() { return personalityScore; }
    public String getPersonalityType() { return personalityType; }
    public void setPersonalityType(String t) { this.personalityType = t; }

    // Method to output participant data for a team in CSV format
    public String toCSVForTeam(int teamId) {
        return String.format("%d,%s,%s,%d,%s,%s,%d,%s",
                teamId, id, name, skillLevel, interest, role, personalityScore, personalityType);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Role:%s Interest:%s Skill:%d Personality:%s/%d",
                id, name, role, interest, skillLevel, personalityType, personalityScore);
    }
}
