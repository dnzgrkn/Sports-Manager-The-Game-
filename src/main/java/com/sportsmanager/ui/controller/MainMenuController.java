package com.sportsmanager.ui.controller;

import com.sportsmanager.app.GameSession;
import com.sportsmanager.app.SaveLoadService;
import com.sportsmanager.app.SportRegistry;
import com.sportsmanager.core.Sport;
import com.sportsmanager.core.Team;
import com.sportsmanager.ui.SceneNavigator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Optional;

public class MainMenuController {

    private final SaveLoadService saveLoadService = new SaveLoadService();

    @FXML private ComboBox<String> sportComboBox;
    @FXML private TextField teamNameField;

    @FXML
    public void initialize() {
        List<String> sportNames = SportRegistry.getInstance().getAvailableSportNames();
        sportComboBox.setItems(FXCollections.observableArrayList(sportNames));
        if (!sportNames.isEmpty()) {
            sportComboBox.setValue(sportNames.get(0));
        }
    }

    @FXML
    public void onNewGame() {
        String selectedSportName = sportComboBox.getValue();
        if (selectedSportName == null) {
            showAlert(Alert.AlertType.WARNING, "Uyarı", "Lütfen bir spor seçin.");
            return;
        }

        Sport sport = SportRegistry.getInstance().get(selectedSportName);

        try {
            GameSession.getInstance().startNewGame(sport, "Süper Lig", 20);
        } catch (UnsupportedOperationException e) {
            showAlert(Alert.AlertType.WARNING, "Uyarı",
                    selectedSportName + " için lig modu henüz desteklenmiyor.");
            return;
        }

        List<Team> teams = GameSession.getInstance().getActiveLeague().getTeams();

        Dialog<Team> dialog = new Dialog<>();
        dialog.setTitle("Takım Seç");
        dialog.setHeaderText("Yönetmek istediğiniz takımı seçin:");

        ButtonType selectButton = new ButtonType("Seç", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectButton, ButtonType.CANCEL);

        ListView<Team> listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(teams));
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Team team, boolean empty) {
                super.updateItem(team, empty);
                setText(empty || team == null ? null : team.getName());
            }
        });
        listView.getSelectionModel().selectFirst();
        listView.setPrefHeight(400);
        dialog.getDialogPane().setContent(listView);

        dialog.setResultConverter(buttonType ->
                buttonType == selectButton
                        ? listView.getSelectionModel().getSelectedItem()
                        : null
        );

        Optional<Team> result = dialog.showAndWait();
        result.ifPresent(team -> {
            GameSession.getInstance().setPlayerTeam(team);
            SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE);
        });
    }

    @FXML
    public void onLoadGame() {
        List<String> saves = saveLoadService.listSaves();
        if (saves.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Bilgi", "Kayıtlı oyun bulunamadı.");
            return;
        }

        ChoiceDialog<String> dialog = new ChoiceDialog<>(saves.get(0), saves);
        dialog.setTitle("Oyun Yükle");
        dialog.setHeaderText("Yüklenecek kaydı seçin:");
        dialog.setContentText("Kayıt:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }

        try {
            saveLoadService.loadGame(result.get());
            SceneNavigator.navigateTo(SceneNavigator.Screen.LEAGUE);
        } catch (RuntimeException e) {
            showAlert(Alert.AlertType.ERROR, "Hata", "Kayıt yüklenemedi: " + e.getMessage());
        }
    }

    @FXML
    public void onQuit() {
        Platform.exit();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
