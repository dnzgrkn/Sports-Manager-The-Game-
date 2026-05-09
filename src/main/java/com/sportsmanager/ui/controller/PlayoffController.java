package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.MatchOrchestrator;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.LeagueRecord;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayoffController {

    @FXML private Label roundLabel;
    @FXML private ListView<Fixture> matchupListView;
    @FXML private Label statusLabel;

    private List<Fixture> currentRoundFixtures = new ArrayList<>();

    // Playoff week offsets (not in league fixture list)
    private static final int QF_WEEK  = 1000;
    private static final int SF_WEEK  = 1001;
    private static final int FIN_WEEK = 1002;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();

        setupListView();

        if (session.getPlayoffFixtures() == null) {
            // First entry: build quarterfinals
            session.setInPlayoff(true);
            session.setPlayoffRound("QUARTERFINAL");
            buildQuarterfinals(session);
        } else {
            // Returning from a match: restore current round
            currentRoundFixtures = session.getPlayoffFixtures();
        }

        refreshUI();
    }

    @FXML
    public void onAdvancePlayoff() {
        // 1. Is there an unplayed player fixture?
        Fixture playerFixture = findPlayerFixture();
        if (playerFixture != null) {
            GameSession.getInstance().setCurrentPlayerFixture(playerFixture);
            SceneNavigator.navigateTo(SceneNavigator.Screen.PRE_MATCH);
            return;
        }

        // 2. Simulate remaining unplayed AI fixtures
        MatchOrchestrator orchestrator = new MatchOrchestrator();
        for (Fixture f : currentRoundFixtures) {
            if (!f.isPlayed()) {
                orchestrator.runAIMatch(f);
            }
        }

        // 3. Check if round is complete
        boolean allPlayed = currentRoundFixtures.stream().allMatch(Fixture::isPlayed);
        if (!allPlayed) {
            refreshUI();
            return;
        }

        // 4. Advance round
        String round = GameSession.getInstance().getPlayoffRound();
        switch (round) {
            case "QUARTERFINAL" -> advanceToSemifinal();
            case "SEMIFINAL"    -> advanceToFinal();
            case "FINAL"        -> endPlayoff();
        }
    }

    @FXML
    public void onMainMenu() {
        GameSession.reset();
        SceneNavigator.navigateTo(SceneNavigator.Screen.MAIN_MENU);
    }

    // ── Private helpers ───────────────────────────────────────

    private void buildQuarterfinals(GameSession session) {
        League league = session.getActiveLeague();
        List<LeagueRecord> table = league.getTable();
        Map<UUID, Team> teamMap = league.getTeams().stream()
                .collect(Collectors.toMap(Team::getId, t -> t));

        List<Team> top8 = table.stream()
                .limit(8)
                .map(r -> teamMap.get(r.getTeamId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // Pad to 8 if fewer teams
        while (top8.size() < 8) top8.add(top8.get(0));

        currentRoundFixtures = new ArrayList<>();
        currentRoundFixtures.add(new Fixture(top8.get(0), top8.get(7), QF_WEEK));
        currentRoundFixtures.add(new Fixture(top8.get(1), top8.get(6), QF_WEEK));
        currentRoundFixtures.add(new Fixture(top8.get(2), top8.get(5), QF_WEEK));
        currentRoundFixtures.add(new Fixture(top8.get(3), top8.get(4), QF_WEEK));

        session.setPlayoffFixtures(currentRoundFixtures);
    }

    private void advanceToSemifinal() {
        // Winners: QF1 winner vs QF4 winner, QF2 winner vs QF3 winner
        Team w0 = getWinner(currentRoundFixtures.get(0));
        Team w1 = getWinner(currentRoundFixtures.get(1));
        Team w2 = getWinner(currentRoundFixtures.get(2));
        Team w3 = getWinner(currentRoundFixtures.get(3));

        currentRoundFixtures = new ArrayList<>();
        currentRoundFixtures.add(new Fixture(w0, w3, SF_WEEK));
        currentRoundFixtures.add(new Fixture(w1, w2, SF_WEEK));

        GameSession session = GameSession.getInstance();
        session.setPlayoffRound("SEMIFINAL");
        session.setPlayoffFixtures(currentRoundFixtures);
        refreshUI();
    }

    private void advanceToFinal() {
        Team w0 = getWinner(currentRoundFixtures.get(0));
        Team w1 = getWinner(currentRoundFixtures.get(1));

        currentRoundFixtures = new ArrayList<>();
        currentRoundFixtures.add(new Fixture(w0, w1, FIN_WEEK));

        GameSession session = GameSession.getInstance();
        session.setPlayoffRound("FINAL");
        session.setPlayoffFixtures(currentRoundFixtures);
        refreshUI();
    }

    private void endPlayoff() {
        Team champion = getWinner(currentRoundFixtures.get(0));
        statusLabel.setText("ŞAMPIYON: " + champion.getName().toUpperCase());
        GameSession.getInstance().setInPlayoff(false);
        // Navigate to season end after a brief display
        javafx.application.Platform.runLater(() ->
            SceneNavigator.navigateTo(SceneNavigator.Screen.SEASON_END));
    }

    private Team getWinner(Fixture fixture) {
        MatchResult result = fixture.getResult();
        if (result != null && result.isHomeWin()) return fixture.getHomeTeam();
        return fixture.getAwayTeam();
    }

    private Fixture findPlayerFixture() {
        Team playerTeam = GameSession.getInstance().getPlayerTeam();
        return currentRoundFixtures.stream()
                .filter(f -> !f.isPlayed())
                .filter(f -> f.getHomeTeam().getId().equals(playerTeam.getId())
                          || f.getAwayTeam().getId().equals(playerTeam.getId()))
                .findFirst()
                .orElse(null);
    }

    private void refreshUI() {
        String round = GameSession.getInstance().getPlayoffRound();
        roundLabel.setText(switch (round) {
            case "QUARTERFINAL" -> "ÇEYREK FİNAL";
            case "SEMIFINAL"    -> "YARI FİNAL";
            case "FINAL"        -> "FİNAL";
            default             -> round;
        });

        matchupListView.getItems().setAll(currentRoundFixtures);

        // Status
        Team playerTeam = GameSession.getInstance().getPlayerTeam();
        boolean playerInRound = currentRoundFixtures.stream().anyMatch(f ->
                f.getHomeTeam().getId().equals(playerTeam.getId())
             || f.getAwayTeam().getId().equals(playerTeam.getId()));

        if (playerInRound) {
            Fixture pf = findPlayerFixture();
            if (pf != null) {
                statusLabel.setText("Maçını oynamak için 'Haftayı İlerlet'e bas.");
            } else {
                statusLabel.setText("Maçın oynandı. Diğer sonuçlar için ilerlemeye devam et.");
            }
        } else {
            statusLabel.setText("Takımın elendi. Turnuvayı izlemeye devam et.");
        }
    }

    private void setupListView() {
        Team playerTeam = GameSession.getInstance().getPlayerTeam();
        matchupListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Fixture f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                String home = f.getHomeTeam().getName();
                String away = f.getAwayTeam().getName();
                boolean isPlayerMatch =
                        f.getHomeTeam().getId().equals(playerTeam.getId())
                     || f.getAwayTeam().getId().equals(playerTeam.getId());

                if (f.isPlayed() && f.getResult() != null) {
                    MatchResult r = f.getResult();
                    setText(home + "  " + r.getHomeScore() + " - " + r.getAwayScore() + "  " + away);
                    setStyle("-fx-text-fill: #eaeaea; -fx-font-family: 'Courier New';"
                           + " -fx-font-size: 13px; -fx-opacity: 0.5;"
                           + " -fx-padding: 8 16;");
                } else if (isPlayerMatch) {
                    setText("▶  " + home + "  vs  " + away + "  ◀");
                    setStyle("-fx-text-fill: #e94560; -fx-font-family: 'Courier New';"
                           + " -fx-font-size: 14px; -fx-font-weight: bold;"
                           + " -fx-background-color: #1a0a0f; -fx-padding: 8 16;");
                } else {
                    setText(home + "  vs  " + away);
                    setStyle("-fx-text-fill: #eaeaea; -fx-font-family: 'Courier New';"
                           + " -fx-font-size: 13px; -fx-padding: 8 16;");
                }
            }
        });
    }
}
