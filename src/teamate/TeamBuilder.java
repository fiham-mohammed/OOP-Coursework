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

    public List<Team> formTeams() {
        List<Team> teams = new ArrayList<>();
        if (participants.isEmpty()) return teams;

        int expectedTeams = (participants.size() + teamSize - 1) / teamSize;
        for (int i = 1; i <= expectedTeams; i++) teams.add(new Team(i));

        Collections.shuffle(participants, new Random(System.currentTimeMillis()));

        // Greedy assignment optimizing diversity
        for (Participant p : participants) {
            Team best = null;
            int bestScore = Integer.MIN_VALUE;
            for (Team t : teams) {
                if (t.size() >= teamSize) continue;
                int score = evaluateFit(t, p);
                if (score > bestScore) { bestScore = score; best = t; }
            }
            if (best == null) {
                best = new Team(teams.size() + 1);
                teams.add(best);
            }
            best.addMember(p);
        }

        // Post-process to try to ensure required roles in each team
        enforceRequiredRoles(teams);

        teams.removeIf(t -> t.size() == 0);
        return teams;
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
