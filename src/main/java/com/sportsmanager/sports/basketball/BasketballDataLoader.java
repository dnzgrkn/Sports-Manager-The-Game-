package com.sportsmanager.sports.basketball;

import com.sportsmanager.core.*;
import java.util.*;

public class BasketballDataLoader {
    private final Sport basketballSport;
    private final Random random = new Random();

    public BasketballDataLoader(Sport basketballSport) {
        this.basketballSport = basketballSport;
    }

    public League generateLeague(String leagueName, int teamCount) {
        List<Team> teams = new ArrayList<>();
        List<String> names = new ArrayList<>(Arrays.asList(
                "Ankara Wolves", "İzmir Eagles", "İstanbul Bulls", "Bursa Bears", "Adana Lions",
                "Antalya Sharks", "Eskişehir Owls", "Trabzon Storm", "Konya Hawks", "Gaziantep Tigers",
                "Mersin Dolphins", "Diyarbakır Dragons", "Samsun Falcons", "Kayseri Panthers", "Denizli Roosters",
                "Muğla Knights", "Sakarya Warriors", "Kocaeli Pirates", "Aydın Wizards", "Urfa Scorpions"
        ));
        Collections.shuffle(names);

        for (int i = 0; i < Math.min(teamCount, names.size()); i++) {
            String teamName = names.get(i);
            Team team = new Team(teamName, teamName.substring(0, 3).toLowerCase() + "_logo.png");
            addPlayersToTeam(team);
            teams.add(team);
        }

        League league = new League(leagueName, teams, basketballSport);
        league.generateFixtures();
        return league;
    }

    private void addPlayersToTeam(Team team) {
        String[] roles = {"PG", "PG", "SG", "SG", "SF", "SF", "PF", "PF", "C", "C", "PG", "C"}; // 12 players
        for (String role : roles) {
            team.addPlayer(createPositionedPlayer(role, team.getName()));
        }
    }

    private Player createPositionedPlayer(String role, String teamName) {
        PlayerAttributeSchema schema = basketballSport.getAttributeSchema();


        int age = 19 + random.nextInt(16);
        BasketballPlayer player = new BasketballPlayer("Player " + role + " - " + teamName, age, role);

        Map<String, Integer> attributes = new HashMap<>();
        for (String attrName : schema.getAttributeNames()) {
            attributes.put(attrName, schema.generateRandom(attrName));
        }


        if (role.equals("C")) {
            if (attributes.get("speed") > 70) attributes.put("speed", 70); // C için speed max 70
        } else if (role.equals("PG")) {
            if (attributes.get("rebounding") > 60) attributes.put("rebounding", 60); // PG için rebounding max 60
        }

        player.setAttributes(attributes);
        return player;
    }
}