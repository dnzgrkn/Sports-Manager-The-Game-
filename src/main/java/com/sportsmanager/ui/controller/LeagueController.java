package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.LeagueOrchestrator;
import com.sportsmanager.app.SaveLoadService;
import com.sportsmanager.app.TrainingOrchestrator;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.LeagueRecord;
import com.sportsmanager.core.MatchResult;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.PlayerAttributeSchema;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    private final SaveLoadService saveLoadService = new SaveLoadService();
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

        squadList.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Player selected = squadList.getSelectionModel().getSelectedItem();
                if (selected != null) showPlayerDetailDialog(selected);
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
        showTrainingDialog();
    }

    @FXML
    public void onSaveGame() {
        TextInputDialog dialog = new TextInputDialog("save-" + System.currentTimeMillis());
        dialog.setTitle("Oyunu Kaydet");
        dialog.setHeaderText("Kayıt adı girin:");
        dialog.setContentText("Kayıt:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            saveLoadService.saveGame(result.get());
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Oyun kaydedildi.");
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Kayıt başarısız: " + e.getMessage());
        }
    }

    @FXML
    public void onLoadGame() {
        List<String> saves = saveLoadService.listSaves();
        if (saves.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Kayıtlı oyun bulunamadı.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(saves.get(0), saves);
        dialog.setTitle("Oyunu Yükle");
        dialog.setHeaderText("Yüklenecek kaydı seçin:");
        dialog.setContentText("Kayıt:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return;

        try {
            saveLoadService.loadGame(result.get());
            orchestrator = new LeagueOrchestrator();
            refreshAll();
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Kayıt yüklenemedi: " + e.getMessage());
        }
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

    // ── Private helpers ───────────────────────────────────────

    private void showPlayerDetailDialog(Player player) {
        GameSession session = GameSession.getInstance();
        Sport sport = session.getActiveSport();
        PlayerAttributeSchema schema = sport.getAttributeSchema();

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(player.getName());
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("""
                -fx-background-color: #0d0d1a;
                -fx-border-color: #e94560;
                -fx-border-width: 1;
                -fx-padding: 0;
                """);
        pane.setPrefWidth(440);

        VBox content = new VBox(14);
        content.setStyle("-fx-padding: 24 28 16 28; -fx-background-color: #0d0d1a;");

        // Name
        Label nameLabel = new Label(player.getName());
        nameLabel.setStyle("""
                -fx-text-fill: #ffffff;
                -fx-font-family: 'Impact';
                -fx-font-size: 30px;
                -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.12), 8, 0.2, 0, 0);
                """);

        // Badges row
        HBox badges = new HBox(10);
        badges.setAlignment(Pos.CENTER_LEFT);
        Label posBadge = new Label(player.getPosition());
        posBadge.setStyle("""
                -fx-text-fill: #e94560;
                -fx-font-family: 'Courier New';
                -fx-font-size: 11px;
                -fx-font-weight: bold;
                -fx-padding: 3 8;
                -fx-border-color: #e94560;
                -fx-border-width: 1;
                """);
        Label ageBadge = new Label(player.getAge() + " yaş");
        ageBadge.setStyle("""
                -fx-text-fill: #eaeaea;
                -fx-font-family: 'Courier New';
                -fx-font-size: 11px;
                -fx-opacity: 0.55;
                """);
        badges.getChildren().addAll(posBadge, ageBadge);

        // Overall rating
        HBox ratingRow = new HBox(12);
        ratingRow.setAlignment(Pos.CENTER_LEFT);
        Label ratingTitle = new Label("GENEL RATING");
        ratingTitle.setStyle("""
                -fx-text-fill: #eaeaea;
                -fx-font-family: 'Courier New';
                -fx-font-size: 10px;
                -fx-opacity: 0.38;
                -fx-pref-width: 100;
                """);
        Label ratingValue = new Label(String.valueOf(player.computeOverallRating()));
        ratingValue.setStyle("""
                -fx-text-fill: #e94560;
                -fx-font-family: 'Courier New';
                -fx-font-size: 26px;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #e94560, 10, 0.3, 0, 0);
                """);
        ratingRow.getChildren().addAll(ratingTitle, ratingValue);

        // Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #e94560; -fx-opacity: 0.18;");

        // Attributes
        VBox attrBox = new VBox(9);
        for (Map.Entry<String, Integer> entry : player.getAttributes().entrySet()) {
            String attrName = entry.getKey();
            int value = entry.getValue();
            int[] range = schema.getRange(attrName);
            double progress = (range != null && range[1] > range[0])
                    ? (double) (value - range[0]) / (range[1] - range[0])
                    : value / 100.0;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);

            Label attrLabel = new Label(attrName.toUpperCase());
            attrLabel.setMinWidth(110);
            attrLabel.setStyle("""
                    -fx-text-fill: #eaeaea;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 10px;
                    -fx-opacity: 0.5;
                    """);

            ProgressBar bar = new ProgressBar(Math.max(0, Math.min(1, progress)));
            bar.setPrefWidth(190);
            bar.setPrefHeight(8);
            bar.setStyle("-fx-accent: #4a9eff;");

            Label valLabel = new Label(String.valueOf(value));
            valLabel.setMinWidth(30);
            valLabel.setStyle("""
                    -fx-text-fill: #eaeaea;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 12px;
                    -fx-font-weight: bold;
                    """);

            row.getChildren().addAll(attrLabel, bar, valLabel);
            attrBox.getChildren().add(row);
        }

        // Injury status
        Region injDivider = new Region();
        injDivider.setPrefHeight(1);
        injDivider.setStyle("-fx-background-color: #eaeaea; -fx-opacity: 0.08;");

        Label injLabel;
        if (!player.isAvailable()) {
            injLabel = new Label("SAKATI  —  " + player.getInjuredForGames() + " maç dışında kalacak");
            injLabel.setStyle("""
                    -fx-text-fill: #e94560;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 12px;
                    -fx-font-weight: bold;
                    """);
        } else {
            injLabel = new Label("SAĞLIKLI");
            injLabel.setStyle("""
                    -fx-text-fill: #22c55e;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 12px;
                    -fx-font-weight: bold;
                    """);
        }

        content.getChildren().addAll(nameLabel, badges, ratingRow, divider, attrBox, injDivider, injLabel);
        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) pane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.setStyle("""
                    -fx-background-color: #0f3460;
                    -fx-text-fill: #eaeaea;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 12px;
                    -fx-background-radius: 0;
                    -fx-cursor: hand;
                    -fx-padding: 6 20;
                    """);
        }

        dialog.showAndWait();
    }

    private void showTrainingDialog() {
        GameSession session = GameSession.getInstance();
        int currentWeek = session.getCurrentWeek();

        if (session.getLastTrainedWeek() == currentWeek && currentWeek >= 0
                && session.getLastTrainedWeek() != -1) {
            showAlert(Alert.AlertType.INFORMATION, "Antrenman",
                    "Bu hafta zaten antrenman yaptınız. Sonraki haftada tekrar kullanabilirsiniz.");
            return;
        }

        Sport sport = session.getActiveSport();
        TrainingOrchestrator trainingOrchestrator = new TrainingOrchestrator();
        List<String> attributes = trainingOrchestrator.getTrainableAttributes(sport);

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Antrenman");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.setStyle("""
                -fx-background-color: #0d0d1a;
                -fx-border-color: #0f3460;
                -fx-border-width: 1;
                -fx-padding: 0;
                """);
        pane.setPrefWidth(400);

        VBox content = new VBox(16);
        content.setStyle("-fx-padding: 24 28 16 28; -fx-background-color: #0d0d1a;");

        Label title = new Label("ANTRENMAN");
        title.setStyle("""
                -fx-text-fill: #4a9eff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 20px;
                -fx-font-weight: bold;
                -fx-effect: dropshadow(gaussian, #4a9eff, 12, 0.3, 0, 0);
                """);

        Label hint = new Label("Geliştirmek istediğiniz özelliği seçin.\nTüm uygun oyuncular 1-3 puan kazanır.");
        hint.setStyle("""
                -fx-text-fill: #eaeaea;
                -fx-font-family: 'Courier New';
                -fx-font-size: 11px;
                -fx-opacity: 0.5;
                -fx-wrap-text: true;
                """);

        ComboBox<String> attrCombo = new ComboBox<>(FXCollections.observableArrayList(attributes));
        attrCombo.setPromptText("Özellik seçin...");
        attrCombo.setPrefWidth(344);
        attrCombo.setPrefHeight(40);
        attrCombo.setStyle("""
                -fx-background-color: #16213e;
                -fx-background-radius: 0;
                -fx-border-radius: 0;
                """);

        Button applyBtn = new Button("Antrenmanı Uygula");
        applyBtn.setPrefWidth(344);
        applyBtn.setPrefHeight(42);
        applyBtn.setStyle("""
                -fx-background-color: #4a9eff;
                -fx-text-fill: #ffffff;
                -fx-font-family: 'Courier New';
                -fx-font-size: 13px;
                -fx-font-weight: bold;
                -fx-background-radius: 0;
                -fx-cursor: hand;
                -fx-effect: dropshadow(gaussian, #4a9eff, 16, 0.3, 0, 4);
                """);

        Label resultLabel = new Label();
        resultLabel.setStyle("""
                -fx-text-fill: #22c55e;
                -fx-font-family: 'Courier New';
                -fx-font-size: 12px;
                -fx-font-weight: bold;
                """);
        resultLabel.setVisible(false);
        resultLabel.setManaged(false);

        applyBtn.setOnAction(e -> {
            String selected = attrCombo.getValue();
            if (selected == null) return;
            trainingOrchestrator.applyTraining(session.getPlayerTeam(), selected);
            session.setLastTrainedWeek(currentWeek);
            resultLabel.setText("✓  " + selected.toUpperCase() + " antrenmanı tamamlandı!");
            resultLabel.setVisible(true);
            resultLabel.setManaged(true);
            applyBtn.setDisable(true);
            attrCombo.setDisable(true);
        });

        content.getChildren().addAll(title, hint, attrCombo, applyBtn, resultLabel);
        pane.setContent(content);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeBtn = (Button) pane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.setStyle("""
                    -fx-background-color: #0f3460;
                    -fx-text-fill: #eaeaea;
                    -fx-font-family: 'Courier New';
                    -fx-font-size: 12px;
                    -fx-background-radius: 0;
                    -fx-cursor: hand;
                    -fx-padding: 6 20;
                    """);
        }

        dialog.showAndWait();
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
