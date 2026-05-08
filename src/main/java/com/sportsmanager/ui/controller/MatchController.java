package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.MatchOrchestrator;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.GoalEvent;
import com.sportsmanager.core.InjuryEvent;
import com.sportsmanager.core.MinuteEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class MatchController {

    @FXML private Label periodLabel;
    @FXML private Label scoreLabel;
    @FXML private Label minuteLabel;
    @FXML private Label homePossLabel;
    @FXML private Label awayPossLabel;
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

    private int liveHome = 0;
    private int liveAway = 0;
    private int homePoss = 50;
    private int awayPoss = 50;
    private boolean matchFinished = false;

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

        liveHome = liveAway = 0;
        homePoss = awayPoss = 50;

        periodLabel.setText(getPeriodLabel(0) + " / " + totalPeriods);
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

        matchTask.setOnSucceeded(e -> {});
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
        if (matchFinished) {
            periodEndPanel.setVisible(false);
            periodEndPanel.setManaged(false);
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    new com.sportsmanager.app.LeagueOrchestrator()
                            .completeWeekAfterPlayerMatch();
                    return null;
                }
            };
            task.setOnSucceeded(e ->
                SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE));
            task.setOnFailed(e ->
                task.getException().printStackTrace());
            Thread t = new Thread(task);
            t.setDaemon(true);
            t.start();
        } else {
            periodEndPanel.setVisible(false);
            periodEndPanel.setManaged(false);
            periodPause.release();
        }
    }

    // ── Private helpers ───────────────────────────────────────

    private void onMatchEvent(MatchEvent event) {
        switch (event.getType()) {
            case GOAL -> {
                GoalEvent g = (GoalEvent) event;
                String desc = g.getDescription();
                String homeName = fixture.getHomeTeam().getName();
                boolean isHomeGoal = desc.contains("(" + homeName + ")");
                if (isHomeGoal) liveHome++; else liveAway++;

                if (isHomeGoal) {
                    homePoss = Math.min(85, homePoss + 5);
                    awayPoss = 100 - homePoss;
                } else {
                    awayPoss = Math.min(85, awayPoss + 5);
                    homePoss = 100 - awayPoss;
                }

                int snapHome = liveHome, snapAway = liveAway;
                int snapHP = homePoss, snapAP = awayPoss;

                Platform.runLater(() -> {
                    String home = fixture.getHomeTeam().getName();
                    String away = fixture.getAwayTeam().getName();
                    scoreLabel.setText(home + "  " + snapHome + " - " + snapAway + "  " + away);
                    minuteLabel.setText(g.getMinute() + "'");
                    homePossLabel.setText(snapHP + "%");
                    awayPossLabel.setText(snapAP + "%");
                    appendLog("[#22c55e]⚽  " + g.getDescription());
                });
            }
            case INJURY -> {
                InjuryEvent inj = (InjuryEvent) event;
                Platform.runLater(() -> appendLog("[#e94560]🚑  " + inj.getDescription()));
            }
            case MINUTE -> {
                MinuteEvent me = (MinuteEvent) event;
                Platform.runLater(() -> minuteLabel.setText(me.getMinute() + "'"));
            }
            case PERIOD_END -> {
                PeriodEndEvent pe = (PeriodEndEvent) event;
                int periodNum = pe.getPeriodIndex() + 1;
                boolean isLastPeriod = periodNum >= totalPeriods;

                liveHome = pe.getHomeScore();
                liveAway = pe.getAwayScore();
                int periodEndMinute = periodNum * (totalPeriods == 4 ? 10 : 45);

                Platform.runLater(() -> {
                    String home = fixture.getHomeTeam().getName();
                    String away = fixture.getAwayTeam().getName();
                    scoreLabel.setText(home + "  " + pe.getHomeScore() + " - " + pe.getAwayScore() + "  " + away);
                    minuteLabel.setText(periodEndMinute + "'");
                    appendLog("[#f59e0b]─────  " + getPeriodLabel(periodNum - 1) + " Sonu:  "
                            + pe.getHomeScore() + " - " + pe.getAwayScore() + "  ─────");

                    if (isLastPeriod) {
                        matchFinished = true;
                        periodEndLabel.setText("Maç Sona Erdi!  "
                                + pe.getHomeScore() + " - " + pe.getAwayScore());
                        periodEndPanel.setVisible(true);
                        periodEndPanel.setManaged(true);
                    } else {
                        periodLabel.setText(getPeriodLabel(periodNum) + " / " + totalPeriods);
                        periodEndLabel.setText(getPeriodLabel(periodNum - 1) + " Sonu");
                        refreshSubstitutionCombos();
                        periodEndPanel.setVisible(true);
                        periodEndPanel.setManaged(true);
                    }
                });

                try {
                    periodPause.acquire();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
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
        List<Player> squad = playerTeam.getSquad();
        int starterCount = Math.min(squadSize, squad.size());

        // Starters: fixed squad positions 0..starterCount-1 (set by PreMatch reordering)
        List<Player> starters = new ArrayList<>(squad.subList(0, starterCount));
        // Bench: remaining squad positions, only those still available (uninjured)
        List<Player> bench = squad.subList(starterCount, squad.size()).stream()
                .filter(Player::isAvailable)
                .toList();

        outPlayerCombo.setItems(FXCollections.observableArrayList(starters));
        inPlayerCombo.setItems(FXCollections.observableArrayList(bench));
        outPlayerCombo.setValue(null);
        inPlayerCombo.setValue(null);
    }

    private String getPeriodLabel(int periodIndex) {
        if (totalPeriods == 2) {
            return periodIndex == 0 ? "İlk Yarı" : "İkinci Yarı";
        } else if (totalPeriods == 4) {
            return (periodIndex + 1) + ". Çeyrek";
        } else {
            return "Periyot " + (periodIndex + 1);
        }
    }

    private void appendLog(String entry) {
        logItems.add(entry);
        eventLogView.scrollTo(logItems.size() - 1);
    }
}
