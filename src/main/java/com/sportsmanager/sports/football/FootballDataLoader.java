package com.sportsmanager.sports.football;

import com.sportsmanager.core.*;

import java.util.*;

public class FootballDataLoader {

    private static final List<String> TEAM_NAMES = List.of(
            "Kırmızı Kartallar", "Mavi Yıldızlar", "Yeşil Aslanlar", "Sarı Şimşekler",
            "Siyah Kaplanlar", "Beyaz Kartallar", "Altın Boğalar", "Gümüş Kurtlar",
            "Turuncu Alevler", "Mor Ejderhalar", "Pembe Flamingolar", "Kahverengi Ayılar",
            "Gri Geyikler", "Lacivert Dalgalar", "Bordo Akarsu", "Yeşil Orman",
            "Kırmızı Alevler", "Mavi Okyanus", "Altın Kartallar", "Gümüş Ay"
    );

    private static final List<String> FIRST_NAMES = List.of(
            "Ahmet", "Mehmet", "Mustafa", "Ali", "Hasan", "Hüseyin", "İbrahim", "Osman",
            "Yusuf", "Ömer", "Musa", "İsa", "Davut", "Süleyman", "Yunus", "Salih",
            "Kemal", "Erdal", "Serkan", "Burak", "Emre", "Arda", "Caner", "Volkan",
            "Selçuk", "Gökhan", "Mert", "Uğur", "Tolga", "Berk"
    );

    private static final List<String> LAST_NAMES = List.of(
            "Yılmaz", "Kaya", "Demir", "Şahin", "Çelik", "Arslan", "Doğan", "Çetin",
            "Aydın", "Özdemir", "Erdoğan", "Koç", "Güneş", "Bulut", "Polat", "Kılıç",
            "Aslan", "Özcan", "Güler", "Kurt", "Korkmaz", "Yıldız", "Karataş", "Avcı",
            "Kaplan", "Toprak", "Sönmez", "Duman", "Çakır", "Aktaş"
    );

    private static final String[] ALL_POSITIONS = {"GK", "CB", "LB", "RB", "CM", "LM", "RM", "CF"};

    private final Random random = new Random();

    public League generateLeague(String leagueName, int teamCount) {
        FootballSport sport = new FootballSport();
        PlayerAttributeSchema schema = sport.getAttributeSchema();

        List<String> shuffledNames = new ArrayList<>(TEAM_NAMES);
        Collections.shuffle(shuffledNames, random);

        List<Team> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            String teamName = shuffledNames.get(i % shuffledNames.size());
            String logo = "logo" + (i % 10 + 1) + ".png";
            Team team = new Team(teamName, logo);

            // 11 starters: 1 GK, 4 DEF, 4 MID, 2 CF
            for (String pos : starterPositions()) {
                team.addPlayer(createPlayer(pos, schema));
            }
            // 4 random substitutes
            for (int s = 0; s < 4; s++) {
                String pos = ALL_POSITIONS[random.nextInt(ALL_POSITIONS.length)];
                team.addPlayer(createPlayer(pos, schema));
            }

            team.setActiveTactic(FootballTactic.getAll().get(1)); // 4-4-2 Balanced default
            teams.add(team);
        }

        League league = new League(leagueName, teams, sport);
        league.generateFixtures();
        return league;
    }

    private List<String> starterPositions() {
        return List.of("GK", "CB", "CB", "LB", "RB", "CM", "CM", "LM", "RM", "CF", "CF");
    }

    private FootballPlayer createPlayer(String position, PlayerAttributeSchema schema) {
        String name = FIRST_NAMES.get(random.nextInt(FIRST_NAMES.size()))
                + " " + LAST_NAMES.get(random.nextInt(LAST_NAMES.size()));
        int age = 18 + random.nextInt(18); // 18–35

        FootballPlayer player = new FootballPlayer(name, age, position);

        Map<String, Integer> attrs = new HashMap<>();
        for (String attr : schema.getAttributeNames()) {
            int[] base = schema.getRange(attr);
            int[] range = adjustRange(position, attr, base[0], base[1]);
            attrs.put(attr, range[0] + random.nextInt(range[1] - range[0] + 1));
        }
        player.setAttributes(attrs);
        return player;
    }

    private int[] adjustRange(String position, String attr, int min, int max) {
        return switch (position) {
            case "GK" -> switch (attr) {
                case "shooting"  -> new int[]{min, 50};
                case "defending" -> new int[]{60, max};
                case "heading"   -> new int[]{50, max};
                default          -> new int[]{min, max};
            };
            case "CB" -> switch (attr) {
                case "shooting"  -> new int[]{min, 55};
                case "defending" -> new int[]{55, max};
                case "heading"   -> new int[]{50, max};
                default          -> new int[]{min, max};
            };
            case "LB", "RB" -> switch (attr) {
                case "pace"      -> new int[]{50, max};
                case "defending" -> new int[]{45, max};
                default          -> new int[]{min, max};
            };
            case "CM" -> switch (attr) {
                case "passing"  -> new int[]{55, max};
                case "stamina"  -> new int[]{55, max};
                default         -> new int[]{min, max};
            };
            case "LM", "RM" -> switch (attr) {
                case "pace"      -> new int[]{60, max};
                case "dribbling" -> new int[]{50, max};
                default          -> new int[]{min, max};
            };
            case "CF" -> switch (attr) {
                case "defending" -> new int[]{min, 60};
                case "shooting"  -> new int[]{55, max};
                case "pace"      -> new int[]{60, max};
                default          -> new int[]{min, max};
            };
            default -> new int[]{min, max};
        };
    }
}
