package com.sportsmanager.sports.football;

import com.sportsmanager.core.*;

import java.util.List;
import java.util.Random;

public class FootballMatch extends AbstractMatch {

    private final Team homeTeam;
    private final Team awayTeam;
    private final Sport sport;
    private final MatchEventBus eventBus;
    private final Random random;

    public FootballMatch(Team home, Team away, Sport sport, MatchEventBus eventBus) {
        super(home.getName(), away.getName());
        this.homeTeam = home;
        this.awayTeam = away;
        this.sport = sport;
        this.eventBus = eventBus;
        this.random = new Random();
    }

    public FootballMatch(Team home, Team away, Sport sport, MatchEventBus eventBus, long seed) {
        super(home.getName(), away.getName());
        this.homeTeam = home;
        this.awayTeam = away;
        this.sport = sport;
        this.eventBus = eventBus;
        this.random = new Random(seed);
    }

    @Override
    public int getPeriodCount() {
        return 2;
    }

    @Override
    protected void simulatePeriod(int periodIndex) {
        int squadSize = sport.getSquadSize();
        List<Player> homeLineup = homeTeam.getStartingLineup(squadSize);
        List<Player> awayLineup = awayTeam.getStartingLineup(squadSize);

        FootballTactic homeTactic = (FootballTactic) homeTeam.getActiveTactic();
        FootballTactic awayTactic = (FootballTactic) awayTeam.getActiveTactic();
        // Guard: tactic null ise NPE'yi önlemek için periyodu atla
        if (homeTactic == null || awayTactic == null) return;

        double homeAvgRating = averageRating(homeLineup);
        double awayAvgRating = averageRating(awayLineup);

        double injuryProb = sport.getMatchRules().getInjuryProbability();

        for (int tick = 0; tick < 45; tick++) {
            int minute = periodIndex * 45 + tick + 1;

            // 1. Possession
            double homeAttack = homeTactic.getAttackMod() * homeAvgRating;
            double awayAttack = awayTactic.getAttackMod() * awayAvgRating;
            double homePossessionChance = homeAttack / (homeAttack + awayAttack);

            boolean homeHasBall = random.nextDouble() < homePossessionChance;
            Team attackingTeam = homeHasBall ? homeTeam : awayTeam;
            Team defendingTeam = homeHasBall ? awayTeam : homeTeam;
            FootballTactic attackingTactic = homeHasBall ? homeTactic : awayTactic;
            FootballTactic defendingTactic = homeHasBall ? awayTactic : homeTactic;
            List<Player> attackingLineup = homeHasBall ? homeLineup : awayLineup;
            List<Player> defendingLineup = homeHasBall ? awayLineup : homeLineup;
            double attackingAvgRating = homeHasBall ? homeAvgRating : awayAvgRating;
            double defendingAvgRating = homeHasBall ? awayAvgRating : homeAvgRating;

            // 2. Shot chance
            if (random.nextDouble() < 0.12) {
                // 3. Goal chance
                double attackStrength = attackingTactic.getAttackMod() * attackingAvgRating;
                double defenseStrength = defendingTactic.getDefenseMod() * defendingAvgRating;
                double conversionRate = attackStrength / (attackStrength + defenseStrength) * 0.45;

                if (random.nextDouble() < conversionRate) {
                    if (homeHasBall) {
                        homeScore++;
                    } else {
                        awayScore++;
                    }
                    String scorer = attackingLineup.isEmpty()
                            ? attackingTeam.getName()
                            : attackingLineup.get(random.nextInt(attackingLineup.size())).getName();
                    eventBus.publish(new GoalEvent(scorer, minute));
                }
            }

            // 4. Injury check for every player
            for (Player p : homeLineup) {
                if (random.nextDouble() < injuryProb) {
                    int gamesMissed = 1 + random.nextInt(4);
                    p.setInjured(gamesMissed);
                    eventBus.publish(new InjuryEvent(p.getName(), gamesMissed));
                }
            }
            for (Player p : awayLineup) {
                if (random.nextDouble() < injuryProb) {
                    int gamesMissed = 1 + random.nextInt(4);
                    p.setInjured(gamesMissed);
                    eventBus.publish(new InjuryEvent(p.getName(), gamesMissed));
                }
            }
        }
    }

    @Override
    protected void postPeriodHook(int periodIndex) {
        eventBus.publish(new PeriodEndEvent(periodIndex, homeScore, awayScore));
    }

    @Override
    protected MatchResult buildResult() {
        return new MatchResult(homeTeamName, awayTeamName, homeScore, awayScore);
    }

    private double averageRating(List<Player> lineup) {
        if (lineup.isEmpty()) return 0;
        double sum = 0;
        for (Player p : lineup) {
            sum += p.computeOverallRating();
        }
        return sum / lineup.size();
    }
}
