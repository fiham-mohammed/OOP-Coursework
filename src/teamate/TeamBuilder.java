package teamate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * TeamBuilder with:
 * - Greedy placement for diversity
 * - Post-processing to enforce required roles per team if possible
 */
public class TeamBuilder {
    private final List<Participant> participants;
    private final int teamSize;
    // Common roles expected from starter pack; adjust as needed.
    private final List<String> requiredRoles = Arrays.asList("Defender", "Strategist", "Attacker", "Supporter");
    private static final int MAX_THREADS = 10; // Adjust based on system and data size

    public void formTeamsInParallel(List<Participant> participants) {
        // Form teams using threads
        for (Participant participant : participants) {
            Thread thread = new Thread(new TeamFormationTask(participant));  // Each team formation in a separate thread
            thread.start();
        }
    }


    // A Runnable class for forming teams
    static class TeamFormationTask implements Runnable {
        private final Participant participant;

        public TeamFormationTask(Participant participant) {
            this.participant = participant;
        }

        @Override
        public void run() {
            // Logic to form teams based on participant's attributes
            System.out.println("Forming team for participant: " + participant.getName());

            // Example: Simulate forming a team (you can add actual logic here)
            try {
                Thread.sleep(1000);  // Simulate time taken to form the team (replace with actual logic)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Team formed for participant: " + participant.getName());
        }
    }

    public TeamBuilder(List<Participant> participants, int teamSize) {
        this.participants = new ArrayList<>(participants);
        this.teamSize = Math.max(1, teamSize);
    }

    private List<Participant> unassignedParticipants = new ArrayList<>();
    public List<Team> formTeams() {
        List<Team> teams = new ArrayList<>();
        List<Participant> unassignedParticipants = new ArrayList<>();

        int totalParticipants = participants.size();
        int fullTeamsCount = totalParticipants / teamSize;  // Number of full teams
        int leftoverParticipants = totalParticipants % teamSize;  // Number of leftover participants

        // Create full teams
        for (int i = 0; i < fullTeamsCount; i++) {
            teams.add(new Team(i + 1));  // Create full teams
        }

        // Assign participants to full teams first
        int index = 0;
        for (Participant p : participants) {
            if (index < fullTeamsCount * teamSize) {
                // Assign participant to a full team
                Team team = teams.get(index / teamSize);  // Find the appropriate team
                team.addMember(p);
                index++;
            } else {
                // If participant can't be assigned to a team, add to unassigned list
                unassignedParticipants.add(p);
            }
        }

        // If there are leftover participants, only create a team if enough participants exist
        if (leftoverParticipants >= teamSize) {
            Team lastTeam = new Team(teams.size() + 1);
            for (int i = 0; i < leftoverParticipants; i++) {
                lastTeam.addMember(participants.get(fullTeamsCount * teamSize + i));
            }
            teams.add(lastTeam);
        } else {
            // Add all remaining participants to the unassigned list if they can't form a full team
            for (int i = fullTeamsCount * teamSize; i < participants.size(); i++) {
                unassignedParticipants.add(participants.get(i));
            }
        }

        // Set unassigned participants (this list can be used to display unassigned participants)
        setUnassignedParticipants(unassignedParticipants);

        return teams;
    }
    private void setUnassignedParticipants(List<Participant> unassignedParticipants) {
        // Defensive copy for safety
        this.unassignedParticipants = new ArrayList<>(unassignedParticipants);
    }


    private int evaluateFit(Team t, Participant p) {
        int score = 0;
        boolean hasPersonality = t.getMembers().stream()
            .anyMatch(m -> m.getPersonalityType().equalsIgnoreCase(p.getPersonalityType()));
        if (!hasPersonality) score += 6;

        boolean hasRole = t.getMembers().stream()
            .anyMatch(m -> m.getRole().equalsIgnoreCase(p.getRole()));
        if (!hasRole) score += 5;

        boolean hasInterest = t.getMembers().stream()
            .anyMatch(m -> m.getInterest().equalsIgnoreCase(p.getInterest()));
        if (!hasInterest) score += 4;

        // Skill balancing small influence
        if (t.size() > 0) {
            int avg = t.getMembers().stream().mapToInt(Participant::getSkillLevel).sum() / Math.max(1, t.size());
            int diff = Math.abs(avg - p.getSkillLevel());
            score += Math.max(0, 2 - (diff / 5));
        } else {
            score += 2;
        }
        return score;
    }

    private void enforceRequiredRoles(List<Team> teams) {
        // Build pool of participants by role who are not fulfilling required roles yet
        Map<String, List<Participant>> rolePool = participants.stream()
            .collect(Collectors.groupingBy(p -> p.getRole()));

        for (Team t : teams) {
            Set<String> present = t.getMembers().stream().map(Participant::getRole).collect(Collectors.toSet());
            for (String req : requiredRoles) {
                if (t.size() >= teamSize) break;
                if (!present.contains(req) && rolePool.containsKey(req) && !rolePool.get(req).isEmpty()) {
                    Participant candidate = null;
                    for (Participant p : rolePool.get(req)) {
                        if (!t.getMembers().contains(p)) { candidate = p; break; }
                    }
                    if (candidate != null) {
                        t.addMember(candidate);
                        rolePool.get(req).remove(candidate);
                    }
                }
            }
        }
    }

}
