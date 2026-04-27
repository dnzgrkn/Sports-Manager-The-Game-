package com.sportsmanager.ui;

public enum Screen {
    MAIN_MENU("/fxml/MainMenu.fxml"),
    LEAGUE("/fxml/League.fxml");

    private final String fxmlPath;

    Screen(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
