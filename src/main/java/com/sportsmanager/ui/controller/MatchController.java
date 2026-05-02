package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.MatchOrchestrator;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.GoalEvent;
import com.sportsmanager.core.InjuryEvent;
import com.sportsmanager.core.League;
import com.sportsmanager.core.MatchEvent;
import com.sportsmanager.core.MatchEventBus;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.PeriodEndEvent;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.concurrent.Semaphore;

public class MatchController {

    @FXML private Label periodLabel;
    @FXML private Label scoreLabel;
    @FXML private ListView<String> eventLogView;
    @FXML private VBox periodEndPanel;
    @FXML private Label periodEndLabel;
    @FXML private ComboBox<Player> outPlayerCombo;
    @FXML private ComboBox<Player> inPlayerCombo;

    private final ObservableList<String> logItems = FXCollections.observableArrayList();
    private final Semaphore periodPause = new Semaphore(0);

    private Fixture fixture;
    private Team playerTeam;
    private int totalPeriods;
    private int squadSize;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        Sport sport = session.getActiveSport();
        playerTeam = session.getPlayerTeam();
        totalPeriods = sport.getPeriodCount();
        squadSize = sport.getSquadSize();

        League league = session.getActiveLeague();
        fixture = league.getFixturesForWeek(session.getCurrentWeek()).stream()
                .filter(f -> f.getHomeTeam().getId().equals(playerTeam.getId())
                          || f.getAwayTeam().getId().equals(playerTeam.getId()))
                .findFirst()
                .orElse(null);

        if (fixture == null) {
            Platform.runLater(() -> SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE));
            return;
        }

        periodLabel.setText("Periyot 1 / " + totalPeriods);
        scoreLabel.setText(fixture.getHomeTeam().getName() + "  0 - 0  " + fixture.getAwayTeam().getName());

        setupEventLog();
        setupSubstitutionCombos();

        periodEndPanel.setVisible(false);
        periodEndPanel.setManaged(false);

        MatchEventBus eventBus = new MatchEventBus();
        eventBus.subscribe(this::onMatchEvent);

        Task<MatchResult> matchTask = new Task<>() {
            @Override
            protected MatchResult call() {
                return new MatchOrchestrator().runPlayerMatch(fixture, eventBus);
            }
        };

        matchTask.setOnSucceeded(e -> SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE));
        matchTask.setOnFailed(e -> Platform.runLater(() ->
                appendLog("[#e94560]HATA: Maç simülasyonu başarısız.")));

        Thread thread = new Thread(matchTask);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    public void onSubstitute() {
        Player out = outPlayerCombo.getValue();
        Player in  = inPlayerCombo.getValue();
        if (out == null || in == null) return;

        List<Player> squad = playerTeam.getSquad();
        int outIdx = squad.indexOf(out);
        int inIdx  = squad.indexOf(in);
        if (outIdx >= 0 && inIdx >= 0) {
            squad.set(outIdx, in);
            squad.set(inIdx, out);
            playerTeam.makeSubstitution(out, in);
            appendLog("[#4a9eff]↔  " + in.getName() + "  ←  " + out.getName());
            refreshSubstitutionCombos();
        }
    }

    @FXML
    public void onContinue() {
        periodEndPanel.setVisible(false);
        periodEndPanel.setManaged(false);
        periodPause.release();
    }

    // ── Private helpers ───────────────────────────────────────

    private void onMatchEvent(MatchEvent event) {
        switch (event.getType()) {
            case GOAL -> {
                GoalEvent g = (GoalEvent) event;
                Platform.runLater(() -> appendLog("[#22c55e]⚽  " + g.getDescription()));
            }
            case INJURY -> {
                InjuryEvent inj = (InjuryEvent) event;
                Platform.runLater(() -> appendLog("[#e94560]🚑  " + inj.getDescription()));
            }
            case PERIOD_END -> {
                PeriodEndEvent pe = (PeriodEndEvent) event;
                int periodNum = pe.getPeriodIndex() + 1;
                boolean isLastPeriod = periodNum >= totalPeriods;

                Platform.runLater(() -> {
                    String home = fixture.getHomeTeam().getName();
                    String away = fixture.getAwayTeam().getName();
                    scoreLabel.setText(home + "  " + pe.getHomeScore() + " - " + pe.getAwayScore() + "  " + away);
                    appendLog("[#f59e0b]─────  Periyot " + periodNum + " Sonu:  "
                            + pe.getHomeScore() + " - " + pe.getAwayScore() + "  ─────");

                    if (!isLastPeriod) {
                        periodLabel.setText("Periyot " + (periodNum + 1) + " / " + totalPeriods);
                        periodEndLabel.setText("Periyot " + periodNum + " Sonu");
                        refreshSubstitutionCombos();
                        periodEndPanel.setVisible(true);
                        periodEndPanel.setManaged(true);
                    }
                });

                if (!isLastPeriod) {
                    try {
                        periodPause.acquire();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
            default -> {}
        }
    }

    private void setupEventLog() {
        eventLogView.setItems(logItems);
        eventLogView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                if (item.startsWith("[") && item.contains("]")) {
                    int end = item.indexOf("]");
                    String color = item.substring(1, end);
                    setText(item.substring(end + 1));
                    setStyle("-fx-text-fill: " + color + "; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #eaeaea; -fx-font-family: 'Courier New'; -fx-font-size: 13px;");
                }
            }
        });
    }

    private void setupSubstitutionCombos() {
        StringConverter<Player> converter = new StringConverter<>() {
            @Override public String toString(Player p)   { return p != null ? p.getName() + "  ·  " + p.getPosition() : ""; }
            @Override public Player fromString(String s) { return null; }
        };
        outPlayerCombo.setConverter(converter);
        inPlayerCombo.setConverter(converter);
    }

    private void refreshSubstitutionCombos() {
        List<Player> starters = playerTeam.getStartingLineup(squadSize);
        List<Player> bench = playerTeam.getAvailablePlayers().stream()
                .filter(p -> !starters.contains(p))
                .toList();
        outPlayerCombo.setItems(FXCollections.observableArrayList(starters));
        inPlayerCombo.setItems(FXCollections.observableArrayList(bench));
        outPlayerCombo.setValue(null);
        inPlayerCombo.setValue(null);
    }

    private void appendLog(String entry) {
        logItems.add(entry);
        eventLogView.scrollTo(logItems.size() - 1);
    }
}
