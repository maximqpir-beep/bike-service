package ru.mirea.bikeservice.fx.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

final class Alerts {
    private Alerts() {}

    static void error(Window owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Операция не выполнена");
        alert.setContentText(message);
        alert.showAndWait();
    }

    static boolean confirm(Window owner, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.initOwner(owner);
        alert.setTitle("Подтверждение");
        alert.setHeaderText(header);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }
}
