package com.sportsmanager.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class League {
    private final String name;
    private final List<Team> teams;
    private final List<Fixture> fixtures;
    private final Map<UUID, LeagueRecord> standings;
    private int currentWeek;
    private final Sport sport;

    public League(String name, List<Team> teams, Sport sport) {
        this.name = name;
        this.teams = new ArrayList<>(teams);
        this.fixtures = new ArrayList<>();
        this.standings = new LinkedHashMap<>();
        this.currentWeek = 1;
        this.sport = sport;

        for (Team t : teams) {
            standings.put(t.getId(), new LeagueRecord(t.getId(), t.getName()));
        }
    }

    public League(List<Team> teams, Sport sport) {
        this("", teams, sport);
    }

    /**
     * Double round-robin: her takım her diğeriyle hem ev sahibi hem deplasman olarak oynasın.
     * Circle algoritması ile haftalar arasında dengeli dağılım sağlanır.
     */
    public void generateFixtures() {
        fixtures.clear();

        int n = teams.size();
        // Circle algoritması çift sayı gerektirir; tek sayıda "bye" eklenir
        List<Team> rotation = new ArrayList<>(teams);
        if (n % 2 != 0) {
            rotation.add(null); // bye slot
        }
        int size = rotation.size();
        int rounds = size - 1;          // bir tur (first leg) için hafta sayısı
        int matchesPerRound = size / 2;

        // First leg
        for (int round = 0; round < rounds; round++) {
            int week = round + 1;
            for (int i = 0; i < matchesPerRound; i++) {
                Team home = rotation.get(i);
                Team away = rotation.get(size - 1 - i);
                if (home != null && away != null) {
                    fixtures.add(new Fixture(home, away, week));
                }
            }
            // rotation[0] sabit kalır, geri kalanlar saat yönünde döner
            Team last = rotation.remove(size - 1);
            rotation.add(1, last);
        }

        // Second leg: ev sahibi / deplasman yer değiştirir, hafta numarasına rounds eklenir
        int firstLegSize = fixtures.size();
        for (int i = 0; i < firstLegSize; i++) {
            Fixture f = fixtures.get(i);
            fixtures.add(new Fixture(f.getAwayTeam(), f.getHomeTeam(), f.getWeekNumber() + rounds));
        }
    }

    public List<Fixture> getFixturesForWeek(int week) {
        List<Fixture> result = new ArrayList<>();
        for (Fixture f : fixtures) {
            if (f.getWeekNumber() == week) {
                result.add(f);
            }
        }
        return result;
    }

    public void updateStandings(Fixture fixture) {
        if (!fixture.isPlayed()) return;

        MatchResult r = fixture.getResult();
        UUID homeId = fixture.getHomeTeam().getId();
        UUID awayId = fixture.getAwayTeam().getId();
        LeagueRecord homeRecord = standings.get(homeId);
        LeagueRecord awayRecord = standings.get(awayId);

        if (homeRecord == null || awayRecord == null) return;

        if (r.isHomeWin()) {
            homeRecord.recordWin(r.getHomeScore(), r.getAwayScore());
            awayRecord.recordLoss(r.getAwayScore(), r.getHomeScore());
        } else if (r.isDraw()) {
            homeRecord.recordDraw(r.getHomeScore(), r.getAwayScore());
            awayRecord.recordDraw(r.getAwayScore(), r.getHomeScore());
        } else {
            homeRecord.recordLoss(r.getHomeScore(), r.getAwayScore());
            awayRecord.recordWin(r.getAwayScore(), r.getHomeScore());
        }
    }

    public List<LeagueRecord> getTable() {
        List<LeagueRecord> table = new ArrayList<>(standings.values());
        table.sort(sport.getStandingsComparator());
        return table;
    }

    public int getTotalWeeks() {
        int max = 0;
        for (Fixture f : fixtures) {
            if (f.getWeekNumber() > max) max = f.getWeekNumber();
        }
        return max;
    }

    public String getName()                         { return name; }
    public List<Team> getTeams()                    { return teams; }
    public List<Fixture> getFixtures()              { return fixtures; }
    public Map<UUID, LeagueRecord> getStandings()   { return standings; }
    public int getCurrentWeek()                     { return currentWeek; }
    public void setCurrentWeek(int week)            { this.currentWeek = week; }
    public Sport getSport()                         { return sport; }
}
