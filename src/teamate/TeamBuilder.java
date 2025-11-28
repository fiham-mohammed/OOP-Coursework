package teamate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Fixed EnhancedTeamBuilder - ensures each participant is only in one team
 */
public class TeamBuilder {
    private final List<Participant> participants;
    private final int teamSize;

    // Balance constraints from requirements
    private static final int MAX_PER_GAME = 2;
    private static final int MIN_ROLES = 3;
    private static final int MAX_THINKERS = 2;
    private static final int REQUIRED_LEADERS = 1;

    public TeamBuilder(List<Participant> participants, int teamSize) {
        // Create a DEEP COPY to avoid modifying original list
        this.participants = participants != null ?
                participants.stream().map(p -> createCopy(p)).collect(Collectors.toList()) :
                new ArrayList<>();
        this.teamSize = Math.max(1, teamSize);
    }

    /**
     * Create a copy of participant to avoid reference issues
     */
    private Participant createCopy(Participant original) {
        return new Participant(
                original.getId(), original.getName(), original.getEmail(),
                original.getInterest(), original.getSkillLevel(), original.getRole(),
                original.getQ1(), original.getQ2(), original.getQ3(), original.getQ4(), original.getQ5(),
                original.getPersonalityScore(), original.getPersonalityType()
        );
    }

    /**
     * Main method that forms both well-balanced and secondary teams
     * WITHOUT duplicate participants
     */
    public Map<String, Object> formAllTeams() {
        Map<String, Object> result = new HashMap<>();

        if (participants.isEmpty()) {
            result.put("wellBalanced", new ArrayList<Team>());
            result.put("secondary", new ArrayList<Team>());
            result.put("leftover", new ArrayList<Participant>());
            return result;
        }

        // Create working copies to avoid modifying original data
        List<Participant> availableParticipants = new ArrayList<>(this.participants);

        // STEP 1: Form well-balanced teams with strict rules
        Map<String, Object> balancedResult = formWellBalancedTeams(availableParticipants);
        List<Team> wellBalanced = (List<Team>) balancedResult.get("teams");
        List<Participant> leftover = (List<Participant>) balancedResult.get("leftover");

        // STEP 2: Form secondary teams from leftovers
        List<Team> secondaryTeams = formSecondaryTeams(leftover);

        // STEP 3: Update leftover after secondary team formation
        List<Participant> finalLeftover = getUnassignedParticipants(availableParticipants, wellBalanced, secondaryTeams);

        result.put("wellBalanced", wellBalanced);
        result.put("secondary", secondaryTeams);
        result.put("leftover", finalLeftover);

        // VALIDATION: Ensure no duplicates
        validateNoDuplicates(wellBalanced, secondaryTeams);

        return result;
    }

    /**
     * Forms well-balanced teams with strict diversity rules
     */
    private Map<String, Object> formWellBalancedTeams(List<Participant> availableParticipants) {
        Map<String, Object> result = new HashMap<>();
        List<Team> teams = new ArrayList<>();

        // Filter valid participants
        List<Participant> validParticipants = availableParticipants.stream()
                .filter(p -> (p != null) && p.isValid() && p.isEligibleForTeams())
                .collect(Collectors.toList());

        if (validParticipants.isEmpty()) {
            result.put("teams", teams);
            result.put("leftover", new ArrayList<>(availableParticipants));
            return result;
        }

        // Create a working pool that we'll remove from
        List<Participant> workingPool = new ArrayList<>(validParticipants);

        // Categorize by personality type
        List<Participant> leaders = workingPool.stream()
                .filter(p -> "Leader".equals(p.getPersonalityType()))
                .collect(Collectors.toList());
        List<Participant> thinkers = workingPool.stream()
                .filter(p -> "Thinker".equals(p.getPersonalityType()))
                .collect(Collectors.toList());
        List<Participant> balanced = workingPool.stream()
                .filter(p -> "Balanced".equals(p.getPersonalityType()))
                .collect(Collectors.toList());

        // Calculate maximum possible teams
        int maxTeams = validParticipants.size() / teamSize;
        for (int i = 0; i < maxTeams; i++) {
            teams.add(new Team(i + 1));
        }

        if (teams.isEmpty()) {
            result.put("teams", teams);
            result.put("leftover", workingPool);
            return result;
        }

        // PHASE 1: Assign leaders (1 per team)
        for (int i = 0; i < Math.min(leaders.size(), teams.size()); i++) {
            Participant leader = leaders.get(i);
            teams.get(i).addMember(leader);
            workingPool.remove(leader); // REMOVE from available pool
        }

        // PHASE 2: Assign thinkers (1-2 per team)
        for (Team team : teams) {
            int thinkersNeeded = Math.min(MAX_THINKERS, teamSize - team.size());
            for (int i = 0; i < thinkersNeeded; i++) {
                Participant thinker = findCompatibleThinker(thinkers, team, workingPool);
                if (thinker != null) {
                    team.addMember(thinker);
                    workingPool.remove(thinker); // REMOVE from available pool
                    thinkers.remove(thinker); // REMOVE from thinkers list
                }
            }
        }

        // PHASE 3: Fill with balanced participants
        List<Participant> balancedCopy = new ArrayList<>(balanced);
        for (Participant p : balancedCopy) {
            if (!workingPool.contains(p)) continue; // Skip if already assigned

            Team bestTeam = findValidTeam(p, teams, workingPool);
            if (bestTeam != null) {
                bestTeam.addMember(p);
                workingPool.remove(p); // REMOVE from available pool
            }
        }

        // Remove incomplete teams and return their members to the pool
        List<Team> completeTeams = new ArrayList<>();
        for (Team team : teams) {
            if (team.size() == teamSize) {
                completeTeams.add(team);
            } else {
                // Return members of incomplete teams back to pool
                workingPool.addAll(team.getMembers());
                team.getMembers().clear();
            }
        }

        result.put("teams", completeTeams);
        result.put("leftover", workingPool);
        return result;
    }

    /**
     * Forms secondary teams from leftover participants
     */
    private List<Team> formSecondaryTeams(List<Participant> leftover) {
        List<Team> secondaryTeams = new ArrayList<>();
        List<Participant> temp = new ArrayList<>(leftover);
        Collections.shuffle(temp);

        int teamId = 100; // Start secondary teams from ID 100

        while (temp.size() >= teamSize) {
            Team team = new Team(teamId++);
            for (int i = 0; i < teamSize; i++) {
                team.addMember(temp.get(i));
            }
            secondaryTeams.add(team);
            temp.subList(0, teamSize).clear(); // REMOVE assigned participants
        }

        return secondaryTeams;
    }

    /**
     * Get truly unassigned participants after all team formation
     */
    private List<Participant> getUnassignedParticipants(List<Participant> originalPool,
                                                        List<Team> wellBalanced,
                                                        List<Team> secondary) {
        List<Participant> allAssigned = new ArrayList<>();

        // Collect all assigned participants
        wellBalanced.forEach(team -> allAssigned.addAll(team.getMembers()));
        secondary.forEach(team -> allAssigned.addAll(team.getMembers()));

        // Return participants not in any team
        return originalPool.stream()
                .filter(p -> !allAssigned.contains(p))
                .collect(Collectors.toList());
    }

    /**
     * Validate that no participant is in multiple teams
     */
    private void validateNoDuplicates(List<Team> wellBalanced, List<Team> secondary) {
        Set<Participant> allParticipants = new HashSet<>();
        int duplicateCount = 0;

        for (Team team : wellBalanced) {
            for (Participant p : team.getMembers()) {
                if (!allParticipants.add(p)) {
                    duplicateCount++;
                    System.err.println("❌ DUPLICATE: " + p.getId() + " - " + p.getName());
                }
            }
        }

        for (Team team : secondary) {
            for (Participant p : team.getMembers()) {
                if (!allParticipants.add(p)) {
                    duplicateCount++;
                    System.err.println("❌ DUPLICATE: " + p.getId() + " - " + p.getName());
                }
            }
        }

        if (duplicateCount > 0) {
            System.err.println("❌ Found " + duplicateCount + " duplicate participant assignments!");
        } else {
            System.out.println("✅ No duplicate participants found.");
        }
    }

    // ========== UPDATED HELPER METHODS ==========

    private Team findValidTeam(Participant p, List<Team> teams, List<Participant> availablePool) {
        if (!availablePool.contains(p)) return null; // Only consider available participants

        return teams.stream()
                .filter(t -> t.size() < teamSize)
                .filter(t -> countGame(t, p.getInterest()) < MAX_PER_GAME)
                .filter(t -> helpsRoleDiversity(t, p))
                .filter(t -> isPersonalityCompatible(t, p))
                .min(Comparator.comparingInt(t -> evaluateSkillFit(t, p)))
                .orElse(null);
    }

    private Participant findCompatibleThinker(List<Participant> thinkers, Team team, List<Participant> availablePool) {
        return thinkers.stream()
                .filter(t -> availablePool.contains(t)) // Only available thinkers
                .filter(t -> countGame(team, t.getInterest()) < MAX_PER_GAME)
                .filter(t -> helpsRoleDiversity(team, t))
                .findFirst()
                .orElse(null);
    }

    private boolean helpsRoleDiversity(Team t, Participant p) {
        Set<String> roles = t.getMembers().stream()
                .map(Participant::getRole)
                .collect(Collectors.toSet());
        return roles.size() >= MIN_ROLES || !roles.contains(p.getRole());
    }

    private boolean isPersonalityCompatible(Team team, Participant p) {
        int leaders = countLeaders(team);
        int thinkers = countThinkers(team);

        if ("Leader".equals(p.getPersonalityType())) {
            return leaders < REQUIRED_LEADERS;
        } else if ("Thinker".equals(p.getPersonalityType())) {
            return thinkers < MAX_THINKERS;
        }
        return true;
    }

    private int countLeaders(Team t) {
        return (int) t.getMembers().stream()
                .filter(m -> "Leader".equals(m.getPersonalityType()))
                .count();
    }

    private int countThinkers(Team t) {
        return (int) t.getMembers().stream()
                .filter(m -> "Thinker".equals(m.getPersonalityType()))
                .count();
    }

    private int countGame(Team t, String game) {
        return (int) t.getMembers().stream()
                .filter(m -> game.equals(m.getInterest()))
                .count();
    }

    private int evaluateSkillFit(Team t, Participant p) {
        if (t.getMembers().isEmpty()) return 0;

        double avg = t.getMembers().stream()
                .mapToInt(Participant::getSkillLevel)
                .average()
                .orElse(p.getSkillLevel());
        return (int) Math.abs(avg - p.getSkillLevel());
    }

    /**
     * Backward compatibility method
     */
    public List<Team> formTeams() {
        Map<String, Object> result = formAllTeams();
        List<Team> allTeams = new ArrayList<>();
        allTeams.addAll((List<Team>) result.get("wellBalanced"));
        allTeams.addAll((List<Team>) result.get("secondary"));
        return allTeams;
    }
}