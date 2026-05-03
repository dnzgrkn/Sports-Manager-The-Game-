package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.*;
import java.util.List;
import java.util.Random;

public class BasketballMatch extends AbstractMatch {

    private final Team homeTeam;
    private final Team awayTeam;
    private final Sport sport;
    private final MatchEventBus eventBus;
    private final Random random = new Random();

    public BasketballMatch(Team home, Team away, Sport sport, MatchEventBus eventBus) {
        super(home.getName(), away.getName());
        this.homeTeam = home;
        this.awayTeam = away;
        this.sport = sport;
        this.eventBus = eventBus;
    }

    @Override
    public int getPeriodCount() {
        return 4;
    }

    @Override
    protected void simulatePeriod(int periodIndex) {
        int squadSize = sport.getSquadSize();
        List<Player> homeLineup = homeTeam.getStartingLineup(squadSize);
        List<Player> awayLineup = awayTeam.getStartingLineup(squadSize);

        BasketballTactic homeTactic = (BasketballTactic) homeTeam.getActiveTactic();
        BasketballTactic awayTactic = (BasketballTactic) awayTeam.getActiveTactic();

        if (homeTactic == null || awayTactic == null) return;

        double homeAvgRating = averageRating(homeLineup);
        double awayAvgRating = averageRating(awayLineup);
        double injuryProb = sport.getMatchRules().getInjuryProbability();

        for (int tick = 0; tick < 10; tick++) {
            // 1. Calculate Possession
            double homeAttack = homeTactic.getAttackMod() * homeAvgRating;
            double awayAttack = awayTactic.getAttackMod() * awayAvgRating;

            double homePossessionChance = homeAttack / (homeAttack + awayAttack);
            boolean isHomeAttacking = random.nextDouble() < homePossessionChance;

            // 2. Shot attempt (random < 0.35)
            if (random.nextDouble() < 0.35) {
                // 3. Determine shot type and scoring
                double shotRand = random.nextDouble();
                if (shotRand < 0.30) {
                    // 3-pointer
                    addScore(isHomeAttacking, 3);
                } else if (shotRand < 0.80) {
                    // 2-pointer
                    addScore(isHomeAttacking, 2);
                } else if (shotRand < 0.95) {
                    // Free throw
                    addScore(isHomeAttacking, 1);
                }
            }

            // 4. Injury check
            for (Player p : homeLineup) {
                if (random.nextDouble() < injuryProb) {
                    int gamesMissed = 1 + random.nextInt(4);
                    p.setInjured(gamesMissed);
                    if (eventBus != null) {
                        eventBus.publish(new InjuryEvent(p.getName(), gamesMissed));
                    }
                }
            }
            for (Player p : awayLineup) {
                if (random.nextDouble() < injuryProb) {
                    int gamesMissed = 1 + random.nextInt(4);
                    p.setInjured(gamesMissed);
                    if (eventBus != null) {
                        eventBus.publish(new InjuryEvent(p.getName(), gamesMissed));
                    }
                }
            }
        }
    }

    private double averageRating(List<Player> lineup) {
        if (lineup.isEmpty()) return 0;
        double sum = 0;
        for (Player p : lineup) {
            sum += p.computeOverallRating();
        }
        return sum / lineup.size();
    }

    private void addScore(boolean isHome, int points) {
        if (isHome) {
            homeScore += points;
        } else {
            awayScore += points;
        }
    }

    @Override
    protected void postPeriodHook(int periodIndex) {
        // Publish PeriodEndEvent to the event bus
        if (eventBus != null) {
            eventBus.publish(new PeriodEndEvent(periodIndex, homeScore, awayScore));
        }
    }

    @Override
    protected MatchResult buildResult() {
        // Tiebreaker for basketball: no draws allowed, overtime until a winner emerges
        while (homeScore == awayScore) {
            if (random.nextBoolean()) {
                homeScore += 2;
            } else {
                awayScore += 2;
            }
        }
        return new MatchResult(homeTeamName, awayTeamName, homeScore, awayScore);
    }
}