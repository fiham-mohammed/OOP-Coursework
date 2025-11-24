package teamate;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private final int teamID;
    private final List<Participant> members = new ArrayList<>();

    public Team(int teamID) { this.teamID = teamID; }

    public int getTeamID() { return teamID; }
    public List<Participant> getMembers() { return members; }
    public int size() { return members.size(); }
    public void addMember(Participant p) { members.add(p); }

    public String toCSVLines() {
        StringBuilder sb = new StringBuilder();
        for (Participant p : members) sb.append(p.toCSVForTeam(teamID)).append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Team " + teamID + " (size:" + size() + ")\n");
        for (Participant p : members) sb.append("  - ").append(p.toString()).append("\n");
        return sb.toString();
    }
}
