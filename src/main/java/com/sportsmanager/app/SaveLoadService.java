package com.sportsmanager.app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Tactic;
import com.sportsmanager.core.Team;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SaveLoadService {

    public static final String SAVE_DIR = System.getProperty("user.home") + "/SportsManager/saves/";

    private final Gson gson;

    public SaveLoadService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Sport.class, new RuntimeTypeAdapter<>(Sport.class))
                .registerTypeAdapter(Tactic.class, new RuntimeTypeAdapter<>(Tactic.class))
                .registerTypeAdapter(Player.class, new RuntimeTypeAdapter<>(Player.class))
                .registerTypeAdapter(GameSession.class, new GameSessionAdapter())
                .setPrettyPrinting()
                .create();
    }

    public void saveGame(String saveName) {
        validateSaveName(saveName);

        try {
            Files.createDirectories(Path.of(SAVE_DIR));
            String json = gson.toJson(GameSession.getInstance());
            Files.writeString(resolveSavePath(saveName), json);
        } catch (IOException e) {
            throw new IllegalStateException("Could not save game: " + saveName, e);
        }
    }

    public GameSession loadGame(String saveName) {
        validateSaveName(saveName);

        try {
            String json = Files.readString(resolveSavePath(saveName));
            GameSession loaded = gson.fromJson(json, GameSession.class);
            GameSession.reset();
            GameSession session = GameSession.getInstance();
            session.setActiveLeague(loaded.getActiveLeague());
            session.setPlayerTeam(loaded.getPlayerTeam());
            session.setActiveSport(loaded.getActiveSport());
            session.setCurrentWeek(loaded.getCurrentWeek());
            normalizeLoadedSession(session);
            return session;
        } catch (IOException e) {
            throw new IllegalStateException("Could not load game: " + saveName, e);
        }
    }

    public List<String> listSaves() {
        Path directory = Path.of(SAVE_DIR);
        if (!Files.isDirectory(directory)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(path -> path.getFileName().toString().endsWith(".smg"))
                    .map(path -> path.getFileName().toString())
                    .map(name -> name.substring(0, name.length() - 4))
                    .sorted()
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not list saves", e);
        }
    }

    private void normalizeLoadedSession(GameSession session) {
        League league = session.getActiveLeague();
        if (league == null) {
            return;
        }

        if (league.getSport() != null) {
            session.setActiveSport(league.getSport());
        }

        Map<UUID, Team> teamsById = league.getTeams().stream()
                .collect(Collectors.toMap(Team::getId, team -> team, (left, right) -> left));

        Team playerTeam = session.getPlayerTeam();
        if (playerTeam != null) {
            Team canonicalPlayerTeam = teamsById.get(playerTeam.getId());
            if (canonicalPlayerTeam != null) {
                session.setPlayerTeam(canonicalPlayerTeam);
            }
        }

        for (Fixture fixture : league.getFixtures()) {
            Team homeTeam = fixture.getHomeTeam();
            Team awayTeam = fixture.getAwayTeam();
            if (homeTeam != null) {
                Team canonicalHome = teamsById.get(homeTeam.getId());
                if (canonicalHome != null) {
                    setFixtureTeam(fixture, "homeTeam", canonicalHome);
                }
            }
            if (awayTeam != null) {
                Team canonicalAway = teamsById.get(awayTeam.getId());
                if (canonicalAway != null) {
                    setFixtureTeam(fixture, "awayTeam", canonicalAway);
                }
            }
        }
    }

    private void setFixtureTeam(Fixture fixture, String fieldName, Team team) {
        try {
            Field field = Fixture.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(fixture, team);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not relink fixture team", e);
        }
    }

    private Path resolveSavePath(String saveName) {
        return Path.of(SAVE_DIR, saveName + ".smg");
    }

    private void validateSaveName(String saveName) {
        if (saveName == null || saveName.isBlank()) {
            throw new IllegalArgumentException("Save name must not be blank");
        }
    }

    private static final class GameSessionAdapter
            implements JsonSerializer<GameSession>, JsonDeserializer<GameSession> {

        @Override
        public JsonElement serialize(GameSession src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.add("activeLeague", context.serialize(src.getActiveLeague()));
            object.add("playerTeam", context.serialize(src.getPlayerTeam()));
            object.add("activeSport", context.serialize(src.getActiveSport(), Sport.class));
            object.addProperty("currentWeek", src.getCurrentWeek());
            return object;
        }

        @Override
        public GameSession deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            GameSession session = newSessionInstance();
            session.setActiveLeague(context.deserialize(object.get("activeLeague"), League.class));
            session.setPlayerTeam(context.deserialize(object.get("playerTeam"), Team.class));
            session.setActiveSport(context.deserialize(object.get("activeSport"), Sport.class));
            JsonElement currentWeek = object.get("currentWeek");
            session.setCurrentWeek(currentWeek != null ? currentWeek.getAsInt() : 0);
            return session;
        }

        private GameSession newSessionInstance() {
            try {
                Constructor<GameSession> constructor = GameSession.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new JsonParseException("Could not instantiate GameSession", e);
            }
        }
    }

    private static final class RuntimeTypeAdapter<T> implements JsonSerializer<T>, JsonDeserializer<T> {

        private final Class<T> baseType;

        private RuntimeTypeAdapter(Class<T> baseType) {
            this.baseType = baseType;
        }

        @Override
        public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) {
                return JsonNull.INSTANCE;
            }

            JsonObject object = new JsonObject();
            object.addProperty("className", src.getClass().getName());
            object.add("data", context.serialize(src, src.getClass()));
            return object;
        }

        @Override
        public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json == null || json.isJsonNull()) {
                return null;
            }

            JsonObject object = json.getAsJsonObject();
            String className = object.get("className").getAsString();

            try {
                Class<?> implementation = Class.forName(className);
                return baseType.cast(context.deserialize(object.get("data"), implementation));
            } catch (ClassNotFoundException e) {
                throw new JsonParseException("Could not resolve class: " + className, e);
            }
        }
    }
}
