package application.model;

import javafx.scene.control.ButtonBase;

import javafx.scene.layout.Pane;

public class AppearanceUtil {

    public static void setButtonStyleAlpha(ButtonBase ... buttons) {
        for(ButtonBase x : buttons) {
            x.setStyle(null);

            //按钮背景颜色
            x.setStyle("-fx-background-color: TRANSPARENT");
            x.setOnMouseEntered(event -> {
                x.setStyle("-fx-background-color: rgba(0, 0, 0, 0.1)");
            });
            x.setOnMouseExited(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
            x.setOnMousePressed(event -> {
                x.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2)");
            });
            x.setOnMouseReleased(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
        }
    }

    public static void setButtonStyleBeta(ButtonBase ... buttons) {
        for(ButtonBase x : buttons) {
            x.setStyle(null);

            //按钮背景颜色
            x.setStyle("-fx-background-color: TRANSPARENT");
            x.setOnMouseEntered(event -> {
                x.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5)");
            });
            x.setOnMouseExited(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
            x.setOnMousePressed(event -> {
                x.setStyle("-fx-background-color: rgba(127, 127, 127, 0.1)");
            });
            x.setOnMouseReleased(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
        }
    }

    public static void setAppStyle(Pane... pane) {
        for(Pane x : pane) {
            x.setStyle(null);

            //应用图标背景颜色
            x.setStyle("-fx-background-color: TRANSPARENT");
            x.setOnMouseEntered(event -> {
                x.setStyle("-fx-background-color: rgba(0, 0, 0, 0.15)");
            });
            x.setOnMouseExited(event -> {
                x.setStyle("-fx-background-color: TRANSPARENT");
            });
        }
    }
}
