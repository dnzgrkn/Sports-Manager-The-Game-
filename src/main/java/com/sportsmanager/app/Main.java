package com.sportsmanager.app;

import com.sportsmanager.core.*;
import com.sportsmanager.sports.football.*;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        FootballSport sport = new FootballSport();
        SportRegistry.getInstance().register(sport);

        // 1. Create league
        FootballDataLoader loader = new FootballDataLoader();
        League league = loader.generateLeague("Süper Lig", 20);

        System.out.println("=== Sports Manager — Milestone 2 Demo ===");
        System.out.println("League created: " + league.getTeams().size()
                + " teams, " + league.getFixtures().size() + " fixtures");
        System.out.println();

        // 2. Simulate Week 1
        System.out.println("Simulating Week 1...");
        MatchEventBus eventBus = new MatchEventBus();
        List<Fixture> week1 = league.getFixturesForWeek(1);

        for (Fixture fixture : week1) {
            FootballMatch match = new FootballMatch(
                    fixture.getHomeTeam(), fixture.getAwayTeam(), sport, eventBus);
            MatchResult result = match.play();
            fixture.setResult(result);
            league.updateStandings(fixture);

            System.out.printf("  %-22s %d - %d  %s%n",
                    result.getHomeTeamName(),
                    result.getHomeScore(),
                    result.getAwayScore(),
                    result.getAwayTeamName());
        }

        // 3. Standings (top 5)
        System.out.println();
        System.out.println("=== Standings after Week 1 ===");
        List<LeagueRecord> table = league.getTable();
        int limit = Math.min(5, table.size());
        for (int i = 0; i < limit; i++) {
            LeagueRecord r = table.get(i);
            System.out.printf("%d. %-22s - %d pts%n", i + 1, r.getTeamName(), r.getPoints());
        }
    }
}
