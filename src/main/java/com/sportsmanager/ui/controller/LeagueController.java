package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.LeagueOrchestrator;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.LeagueRecord;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class LeagueController {

    @FXML private Label weekLabel;
    @FXML private Label nextOpponentLabel;

    @FXML private TableView<LeagueRecord> standingsTable;
    @FXML private TableColumn<LeagueRecord, Integer> rankColumn;
    @FXML private TableColumn<LeagueRecord, String>  teamColumn;
    @FXML private TableColumn<LeagueRecord, Integer> playedColumn;
    @FXML private TableColumn<LeagueRecord, Integer> winsColumn;
    @FXML private TableColumn<LeagueRecord, Integer> drawsColumn;
    @FXML private TableColumn<LeagueRecord, Integer> lossesColumn;
    @FXML private TableColumn<LeagueRecord, Integer> goalsForColumn;
    @FXML private TableColumn<LeagueRecord, Integer> goalsAgainstColumn;
    @FXML private TableColumn<LeagueRecord, Integer> goalDiffColumn;
    @FXML private TableColumn<LeagueRecord, Integer> pointsColumn;

    @FXML private ListView<Fixture> fixtureList;
    @FXML private ListView<Player>  squadList;

    private LeagueOrchestrator orchestrator;

    @FXML
    public void initialize() {
        orchestrator = new LeagueOrchestrator();
        setupColumns();
        refreshAll();
    }

    private void setupColumns() {
        rankColumn.setCellValueFactory(data -> {
            int rank = standingsTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleIntegerProperty(rank).asObject();
        });

        teamColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTeamName()));
        teamColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setText(null); setStyle(""); return; }
                setText(name);
                Team pt = GameSession.getInstance().getPlayerTeam();
                if (pt != null && name.equals(pt.getName())) {
                    setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 0 0 0 8;");
                } else {
                    setStyle("-fx-text-fill: #eaeaea; -fx-alignment: center-left; -fx-padding: 0 0 0 8;");
                }
            }
        });

        playedColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPlayed()).asObject());
        winsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getWins()).asObject());
        drawsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getDraws()).asObject());
        lossesColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getLosses()).asObject());
        goalsForColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGoalsFor()).asObject());
        goalsAgainstColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGoalsAgainst()).asObject());
        goalDiffColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGoalDifference()).asObject());
        pointsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPoints()).asObject());

        fixtureList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Fixture f, boolean empty) {
                super.updateItem(f, empty);
                if (empty || f == null) { setText(null); setStyle(""); return; }
                String home = f.getHomeTeam() != null ? f.getHomeTeam().getName() : "BYE";
                String away = f.getAwayTeam() != null ? f.getAwayTeam().getName() : "BYE";
                String week = String.format("H%-2d", f.getWeekNumber());
                if (f.isPlayed() && f.getResult() != null) {
                    MatchResult r = f.getResult();
                    setText(week + "  " + home + "  " + r.getHomeScore() + " - " + r.getAwayScore() + "  " + away);
                    setStyle("-fx-opacity: 0.5;");
                } else {
                    setText(week + "  " + home + "  vs  " + away);
                    setStyle("-fx-opacity: 1.0;");
                }
            }
        });

        squadList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Player p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); setStyle(""); return; }
                if (!p.isAvailable()) {
                    setText("⬤ " + p.getName() + "  [" + p.getInjuredForGames() + " maç]");
                    setStyle("-fx-text-fill: #e94560; -fx-font-family: 'Courier New'; -fx-font-size: 11;");
                } else {
                    setText(p.getName() + "  ·  " + p.getPosition());
                    setStyle("-fx-text-fill: #eaeaea; -fx-font-family: 'Courier New'; -fx-font-size: 11;");
                }
            }
        });
    }

    @FXML
    public void onAdvanceWeek() {
        orchestrator.advanceWeek();

        if (orchestrator.isSeasonOver()) {
            SceneNavigator.navigateTo(SceneNavigator.Screen.SEASON_END);
            return;
        }

        if (orchestrator.hasPlayerMatchThisWeek()) {
            SceneNavigator.navigateTo(SceneNavigator.Screen.PRE_MATCH);
        } else {
            refreshAll();
        }
    }

    @FXML
    public void onTraining() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Antrenman");
        alert.setHeaderText(null);
        alert.setContentText("Antrenman ekranı yakında");
        alert.showAndWait();
    }

    public void refreshAll() {
        GameSession session = GameSession.getInstance();
        League league = session.getActiveLeague();
        Team playerTeam = session.getPlayerTeam();

        weekLabel.setText(session.getCurrentWeek() + " / " + league.getTotalWeeks());
        nextOpponentLabel.setText(findNextOpponent(league, playerTeam));

        standingsTable.setItems(FXCollections.observableArrayList(league.getTable()));

        List<Fixture> sorted = league.getFixtures().stream()
                .sorted(Comparator.comparingInt(Fixture::getWeekNumber))
                .collect(Collectors.toList());
        fixtureList.setItems(FXCollections.observableArrayList(sorted));

        if (playerTeam != null) {
            squadList.setItems(FXCollections.observableArrayList(playerTeam.getSquad()));
        }
    }

    private String findNextOpponent(League league, Team playerTeam) {
        if (playerTeam == null) return "-";
        return league.getFixtures().stream()
                .filter(f -> !f.isPlayed())
                .filter(f -> {
                    boolean isHome = f.getHomeTeam() != null
                            && f.getHomeTeam().getId().equals(playerTeam.getId());
                    boolean isAway = f.getAwayTeam() != null
                            && f.getAwayTeam().getId().equals(playerTeam.getId());
                    return isHome || isAway;
                })
                .min(Comparator.comparingInt(Fixture::getWeekNumber))
                .map(f -> {
                    if (f.getHomeTeam() != null && f.getHomeTeam().getId().equals(playerTeam.getId())) {
                        return f.getAwayTeam() != null ? f.getAwayTeam().getName() : "BYE";
                    }
                    return f.getHomeTeam() != null ? f.getHomeTeam().getName() : "BYE";
                })
                .orElse("Sezon bitti");
    }
}
