package teamate;

/**
 * Classifies personality score into categories required by coursework.
 */
public class PersonalityClassifier {
    public String classify(int score) {
        if (score >= 90 && score <= 100) return "Leader";
        if (score >= 70 && score <= 89) return "Balanced";
        if (score >= 50 && score <= 69) return "Thinker";
        if (score >= 0 && score < 50) return "Undefined";
        return "Invalid";
    }
}
