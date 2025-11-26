package teamate;

public abstract class Survey {
    // Common attributes like participant details can go here.
    protected String id;
    protected String name;
    protected String email;

    // Abstract method that all surveys must implement
    public abstract Participant conductSurvey(String filePath);
}

