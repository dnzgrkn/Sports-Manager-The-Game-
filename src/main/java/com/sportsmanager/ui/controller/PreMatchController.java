package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Tactic;
import com.sportsmanager.core.Team;
import com.sportsmanager.sports.basketball.BasketballTactic;
import com.sportsmanager.sports.football.FootballTactic;
import com.sportsmanager.ui.SceneNavigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PreMatchController {

    @FXML private Label homeTeamLabel;
    @FXML private Label awayTeamLabel;
    @FXML private Label squadSizeHintLabel;
    @FXML private Label selectionCountLabel;
    @FXML private Label attackModLabel;
    @FXML private Label defenseModLabel;

    @FXML private ListView<Player> playerListView;
    @FXML private ComboBox<Tactic> tacticComboBox;

    private int squadSize;
    private final Map<Player, BooleanProperty> selectionMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        Sport sport = session.getActiveSport();
        League league = session.getActiveLeague();
        Team playerTeam = session.getPlayerTeam();
        squadSize = sport.getSquadSize();

        // Find this week's fixture for the player
        Fixture fixture = league.getFixturesForWeek(session.getCurrentWeek()).stream()
                .filter(f -> f.getHomeTeam() != null && f.getAwayTeam() != null)
                .filter(f -> f.getHomeTeam().getId().equals(playerTeam.getId())
                          || f.getAwayTeam().getId().equals(playerTeam.getId()))
                .min(Comparator.comparingInt(Fixture::getWeekNumber))
                .orElse(null);

        if (fixture != null) {
            homeTeamLabel.setText(fixture.getHomeTeam().getName());
            awayTeamLabel.setText(fixture.getAwayTeam().getName());
        }

        squadSizeHintLabel.setText(squadSize + " oyuncu seçin");

        // Build player list with default auto-selection
        setupPlayerList(playerTeam.getAvailablePlayers());

        // Build tactic ComboBox
        List<Tactic> tactics = sport.getTactics();
        tacticComboBox.setItems(FXCollections.observableArrayList(tactics));
        tacticComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Tactic t)    { return t != null ? t.getName() : ""; }
            @Override public Tactic fromString(String s)  { return null; }
        });
        if (!tactics.isEmpty()) {
            tacticComboBox.setValue(tactics.get(0));
            updateModifiers(tactics.get(0));
        }
    }

    @FXML
    public void onTacticChanged() {
        updateModifiers(tacticComboBox.getValue());
    }

    @FXML
    public void onStartMatch() {
        List<Player> selected = selectionMap.entrySet().stream()
                .filter(e -> e.getValue().get())
                .map(Map.Entry::getKey)
                .toList();

        if (selected.size() != squadSize) {
            showWarning("Tam olarak " + squadSize + " oyuncu seçmelisiniz. ("
                    + selected.size() + " seçildi)");
            return;
        }

        Tactic tactic = tacticComboBox.getValue();
        if (tactic == null) {
            showWarning("Lütfen bir taktik seçin.");
            return;
        }

        Team playerTeam = GameSession.getInstance().getPlayerTeam();
        playerTeam.setActiveTactic(tactic);

        // Reorder squad so the chosen lineup comes first
        List<Player> squad = playerTeam.getSquad();
        List<Player> reordered = new ArrayList<>(selected);
        squad.stream().filter(p -> !selected.contains(p)).forEach(reordered::add);
        squad.clear();
        squad.addAll(reordered);

        SceneNavigator.navigateTo(SceneNavigator.Screen.MATCH);
    }

    // ── Private helpers ───────────────────────────────────────

    private void setupPlayerList(List<Player> players) {
        selectionMap.clear();
        int autoSelect = 0;
        for (Player p : players) {
            boolean preSelected = autoSelect < squadSize;
            SimpleBooleanProperty prop = new SimpleBooleanProperty(preSelected);
            prop.addListener((obs, old, val) -> refreshSelectionCount());
            selectionMap.put(p, prop);
            if (preSelected) autoSelect++;
        }

        playerListView.setItems(FXCollections.observableArrayList(players));
        playerListView.setCellFactory(CheckBoxListCell.forListView(
                selectionMap::get,
                new StringConverter<>() {
                    @Override
                    public String toString(Player p) {
                        return p != null ? p.getName() + "  ·  " + p.getPosition() : "";
                    }
                    @Override public Player fromString(String s) { return null; }
                }
        ));

        refreshSelectionCount();
    }

    private void refreshSelectionCount() {
        long count = selectionMap.values().stream().filter(BooleanProperty::get).count();
        selectionCountLabel.setText(count + " / " + squadSize + " seçildi");
        if (count == squadSize) {
            selectionCountLabel.setStyle(
                    "-fx-text-fill: #22c55e; -fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-font-weight: bold;");
        } else {
            selectionCountLabel.setStyle(
                    "-fx-text-fill: #e94560; -fx-font-family: 'Courier New'; -fx-font-size: 12; -fx-font-weight: bold;");
        }
    }

    private void updateModifiers(Tactic tactic) {
        if (tactic == null) {
            attackModLabel.setText("—");
            defenseModLabel.setText("—");
            return;
        }
        if (tactic instanceof FootballTactic ft) {
            attackModLabel.setText(String.format("× %.2f", ft.getAttackMod()));
            defenseModLabel.setText(String.format("× %.2f", ft.getDefenseMod()));
        } else if (tactic instanceof BasketballTactic bt) {
            attackModLabel.setText(String.format("× %.2f", bt.getAttackMod()));
            defenseModLabel.setText(String.format("× %.2f", bt.getDefenseMod()));
        } else {
            attackModLabel.setText("—");
            defenseModLabel.setText("—");
        }
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyarı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
