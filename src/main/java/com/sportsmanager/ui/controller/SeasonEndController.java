package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.core.League;
import com.sportsmanager.core.LeagueRecord;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.stream.Collectors;

public class SeasonEndController {

    @FXML private Label championLabel;
    @FXML private Label rankLabel;

    @FXML private TableView<LeagueRecord> standingsTable;
    @FXML private TableColumn<LeagueRecord, Integer> rankColumn;
    @FXML private TableColumn<LeagueRecord, String>  teamColumn;
    @FXML private TableColumn<LeagueRecord, Integer> winsColumn;
    @FXML private TableColumn<LeagueRecord, Integer> drawsColumn;
    @FXML private TableColumn<LeagueRecord, Integer> lossesColumn;
    @FXML private TableColumn<LeagueRecord, Integer> goalDiffColumn;
    @FXML private TableColumn<LeagueRecord, Integer> pointsColumn;

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        League league = session.getActiveLeague();
        Team playerTeam = session.getPlayerTeam();

        List<LeagueRecord> fullTable = league.getTable();
        LeagueRecord champion = fullTable.get(0);

        boolean isChampion = playerTeam != null
                && champion.getTeamId().equals(playerTeam.getId());

        if (isChampion) {
            championLabel.setText("Tebrikler! Şampiyonsunuz!");
            rankLabel.setVisible(false);
            rankLabel.setManaged(false);
        } else {
            championLabel.setText("Şampiyon: " + champion.getTeamName());
            int playerRank = findPlayerRank(fullTable, playerTeam);
            if (playerRank > 0) {
                rankLabel.setText("Siz " + playerRank + ". sıradadınız");
            }
        }

        setupColumns(playerTeam);

        List<LeagueRecord> top5 = fullTable.stream().limit(5).collect(Collectors.toList());
        standingsTable.setItems(FXCollections.observableArrayList(top5));
    }

    @FXML
    public void onNewSeason() {
        GameSession session = GameSession.getInstance();
        League league = session.getActiveLeague();

        for (Team team : league.getTeams()) {
            for (Player player : team.getSquad()) {
                player.setInjured(0);
                player.setFitness(100);
            }
        }

        league.generateFixtures();

        session.setCurrentWeek(0);
        league.setCurrentWeek(0);
        session.setLastTrainedWeek(-1);

        SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE);
    }

    @FXML
    public void onMainMenu() {
        GameSession.reset();
        SceneNavigator.navigateTo(SceneNavigator.Screen.MAIN_MENU);
    }

    private int findPlayerRank(List<LeagueRecord> table, Team playerTeam) {
        if (playerTeam == null) return -1;
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).getTeamId().equals(playerTeam.getId())) {
                return i + 1;
            }
        }
        return -1;
    }

    private void setupColumns(Team playerTeam) {
        rankColumn.setCellValueFactory(data -> {
            int rank = standingsTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleIntegerProperty(rank).asObject();
        });
        rankColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer rank, boolean empty) {
                super.updateItem(rank, empty);
                if (empty || rank == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(rank));
                if (rank == 1) {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: CENTER;");
                } else {
                    setStyle("-fx-text-fill: #eaeaea; -fx-alignment: CENTER;");
                }
            }
        });

        teamColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getTeamName()));
        teamColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String name, boolean empty) {
                super.updateItem(name, empty);
                if (empty || name == null) { setText(null); setStyle(""); return; }
                setText(name);
                boolean isChampionRow = getIndex() == 0;
                boolean isPlayerRow = playerTeam != null && name.equals(playerTeam.getName());
                if (isChampionRow) {
                    setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 0 0 0 8;");
                } else if (isPlayerRow) {
                    setStyle("-fx-text-fill: #e94560; -fx-font-weight: bold; -fx-alignment: center-left; -fx-padding: 0 0 0 8;");
                } else {
                    setStyle("-fx-text-fill: #eaeaea; -fx-alignment: center-left; -fx-padding: 0 0 0 8;");
                }
            }
        });

        winsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getWins()).asObject());
        drawsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getDraws()).asObject());
        lossesColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getLosses()).asObject());
        goalDiffColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getGoalDifference()).asObject());
        pointsColumn.setCellValueFactory(data ->
                new SimpleIntegerProperty(data.getValue().getPoints()).asObject());
    }
}
