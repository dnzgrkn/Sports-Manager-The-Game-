package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.core.Fixture;
import com.sportsmanager.core.League;
import com.sportsmanager.core.Player;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Tactic;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PreMatchController {

    @FXML private Label homeTeamLabel;
    @FXML private Label awayTeamLabel;
    @FXML private Label squadSizeHintLabel;
    @FXML private Label selectionCountLabel;
    @FXML private Label attackModLabel;
    @FXML private Label defenseModLabel;

    @FXML private ListView<Player> playerListView;
    @FXML private ComboBox<Tactic> tacticComboBox;
    @FXML private Canvas lineupCanvas;

    private int squadSize;
    private final Map<Player, BooleanProperty> selectionMap = new LinkedHashMap<>();

    @FXML
    public void initialize() {
        GameSession session = GameSession.getInstance();
        Sport sport = session.getActiveSport();
        League league = session.getActiveLeague();
        Team playerTeam = session.getPlayerTeam();
        squadSize = sport.getSquadSize();

        // Find this week's fixture for the player (playoff fixture takes priority)
        Fixture fixture = session.getCurrentPlayerFixture();
        if (fixture == null) {
            fixture = league.getFixturesForWeek(session.getCurrentWeek()).stream()
                    .filter(f -> f.getHomeTeam() != null && f.getAwayTeam() != null)
                    .filter(f -> f.getHomeTeam().getId().equals(playerTeam.getId())
                              || f.getAwayTeam().getId().equals(playerTeam.getId()))
                    .min(Comparator.comparingInt(Fixture::getWeekNumber))
                    .orElse(null);
        }

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
            drawLineup(tactics.get(0));
        }
    }

    @FXML
    public void onTacticChanged() {
        updateModifiers(tacticComboBox.getValue());
        drawLineup(tacticComboBox.getValue());
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
        try {
            double attack  = (double) tactic.getClass().getMethod("getAttackMod").invoke(tactic);
            double defense = (double) tactic.getClass().getMethod("getDefenseMod").invoke(tactic);
            attackModLabel.setText(String.format("× %.2f", attack));
            defenseModLabel.setText(String.format("× %.2f", defense));
        } catch (Exception e) {
            attackModLabel.setText("—");
            defenseModLabel.setText("—");
        }
    }

    private void drawLineup(Tactic tactic) {
        if (lineupCanvas == null) return;
        double w = lineupCanvas.getWidth();
        double h = lineupCanvas.getHeight();
        GraphicsContext gc = lineupCanvas.getGraphicsContext2D();

        String sportName = GameSession.getInstance().getActiveSport().getName();

        if ("Basketball".equals(sportName)) {
            // Parke zemin — koyu kahverengi
            gc.setFill(Color.web("#6B3A1F"));
            gc.fillRoundRect(0, 0, w, h, 10, 10);

            // Parke deseni — yatay çizgiler
            gc.setStroke(Color.web("#7D4A2A", 0.5));
            gc.setLineWidth(0.8);
            for (int i = 1; i < 12; i++) {
                gc.strokeLine(0, h * i / 12.0, w, h * i / 12.0);
            }

            // Saha sınırı
            gc.setStroke(Color.web("#ffffff", 0.7));
            gc.setLineWidth(2);
            gc.strokeRoundRect(5, 5, w - 10, h - 10, 8, 8);

            // Alt yarı — boyalı alan (dikdörtgen)
            gc.setStroke(Color.web("#ffffff", 0.6));
            gc.setLineWidth(1.5);
            double paintW = w * 0.52;
            double paintH = h * 0.28;
            double paintX = (w - paintW) / 2;
            double paintY = h - paintH - 8;
            gc.strokeRect(paintX, paintY, paintW, paintH);

            // Serbest atış dairesi (boyalı alanın üstünde)
            gc.strokeOval(paintX, paintY - paintH * 0.25, paintW, paintH * 0.5);

            // Üç sayı yayı (alt)
            gc.strokeArc(w * 0.06, h * 0.55, w * 0.88, h * 0.42, 180, 180, ArcType.OPEN);

            // Pota çemberi (alt orta)
            gc.setStroke(Color.web("#FF6B35"));
            gc.setLineWidth(2.5);
            gc.strokeOval(w * 0.40, h - 14, w * 0.20, h * 0.055);

            // Backboard
            gc.setStroke(Color.web("#ffffff", 0.9));
            gc.setLineWidth(3);
            gc.strokeLine(w * 0.32, h - 5, w * 0.68, h - 5);

            // Orta çizgi
            gc.setStroke(Color.web("#ffffff", 0.5));
            gc.setLineWidth(1.5);
            gc.strokeLine(8, h * 0.5, w - 8, h * 0.5);

            // Orta daire
            gc.strokeOval(w * 0.28, h * 0.43, w * 0.44, h * 0.14);

            // Üst yarı — üç sayı yayı
            gc.setStroke(Color.web("#ffffff", 0.3));
            gc.strokeArc(w * 0.06, h * 0.03, w * 0.88, h * 0.42, 0, 180, ArcType.OPEN);

            // 5 oyuncu — forma şeklinde
            List<String> names = selectionMap.entrySet().stream()
                    .filter(e -> e.getValue().get())
                    .map(e -> e.getKey().getName().split(" ")[0])
                    .limit(5)
                    .collect(Collectors.toList());
            while (names.size() < 5) names.add("");

            drawBasketballPlayer(gc, w * 0.50, h * 0.80, "PG", names.get(0), w);
            drawBasketballPlayer(gc, w * 0.15, h * 0.62, "SG", names.get(1), w);
            drawBasketballPlayer(gc, w * 0.85, h * 0.62, "SF", names.get(2), w);
            drawBasketballPlayer(gc, w * 0.28, h * 0.38, "PF", names.get(3), w);
            drawBasketballPlayer(gc, w * 0.72, h * 0.38, "C",  names.get(4), w);
            return;
        }

        // Saha arka planı
        gc.setFill(Color.web("#0a3d1f"));
        gc.fillRoundRect(0, 0, w, h, 12, 12);

        // Çizgiler
        gc.setStroke(Color.web("#ffffff", 0.15));
        gc.setLineWidth(1);
        gc.strokeLine(0, h / 2, w, h / 2);
        gc.strokeOval(w / 2 - 30, h / 2 - 30, 60, 60);
        gc.strokeRect(w * 0.2, h * 0.75, w * 0.6, h * 0.22);
        gc.strokeRect(w * 0.2, h * 0.03, w * 0.6, h * 0.22);

        String tacticName = tactic != null ? tactic.getName() : "4-4-2 Balanced";
        int[] formation = parseFormation(tacticName);

        drawPlayer(gc, w / 2, h * 0.92, "GK", w);

        double[] rowY;
        if (formation.length == 4) {
            rowY = new double[]{h * 0.75, h * 0.55, h * 0.35, h * 0.15};
        } else {
            rowY = new double[]{h * 0.72, h * 0.45, h * 0.18};
        }

        String[] rowLabels = {"DEF", "MID", "FWD", "ATT"};
        for (int row = 0; row < Math.min(formation.length, rowY.length); row++) {
            int count = formation[row];
            for (int i = 0; i < count; i++) {
                double x = w * (i + 1.0) / (count + 1.0);
                drawPlayer(gc, x, rowY[row], rowLabels[row], w);
            }
        }
    }

    private void drawPlayer(GraphicsContext gc, double x, double y,
                            String label, double canvasW) {
        double r = canvasW * 0.055;
        gc.setStroke(Color.web("#e94560"));
        gc.setLineWidth(1.5);
        gc.strokeOval(x - r, y - r, r * 2, r * 2);
        gc.setFill(Color.web("#e94560", 0.25));
        gc.fillOval(x - r, y - r, r * 2, r * 2);
        gc.setFill(Color.web("#ffffff"));
        gc.setFont(javafx.scene.text.Font.font("Courier New", 8));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(label, x, y);
    }

    private void drawBasketballPlayer(GraphicsContext gc,
            double x, double y, String label, String name, double w) {
        double r = w * 0.07;
        // Forma gövdesi (dikdörtgen)
        gc.setFill(Color.web("#e94560", 0.85));
        gc.fillRoundRect(x - r, y - r * 0.8, r * 2, r * 2.2, 6, 6);
        // Forma numarası yeri (beyaz oval)
        gc.setFill(Color.web("#ffffff", 0.9));
        gc.fillOval(x - r * 0.45, y - r * 0.3, r * 0.9, r * 0.9);
        // Pozisyon etiketi
        gc.setFill(Color.web("#e94560"));
        gc.setFont(javafx.scene.text.Font.font("Courier New",
                javafx.scene.text.FontWeight.BOLD, 9));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(label, x, y + r * 0.15);
        // İsim etiketi (altında)
        gc.setFill(Color.web("#ffffff", 0.7));
        gc.setFont(javafx.scene.text.Font.font("Courier New", 8));
        gc.fillText(name, x, y + r * 1.5);
    }

    private int[] parseFormation(String tacticName) {
        String[] parts = tacticName.split(" ");
        if (parts.length > 0) {
            String[] nums = parts[0].split("-");
            try {
                int[] result = new int[nums.length];
                for (int i = 0; i < nums.length; i++) {
                    result[i] = Integer.parseInt(nums[i]);
                }
                return result;
            } catch (NumberFormatException ignored) {}
        }
        return new int[]{4, 4, 2};
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Uyarı");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
