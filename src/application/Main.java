package application;

import application.model.GUI;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        GUI.createSystem(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
