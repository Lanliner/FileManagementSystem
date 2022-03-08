package application.controller;

import application.model.manager.Manager;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class UsageController implements Initializable {

    private static Stage stage = null;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        UsageController.stage = stage;
    }

    @FXML
    private Text usedText;

    @FXML
    private Text idleText;

    @FXML
    private Text downText;

    @FXML
    private GridPane blockGrid;

    @FXML
    private GridPane FATIndexGrid;

    @FXML
    private GridPane FATContGrid;

    /**
     * 读FAT数组，更新使用状况
     */
    public void update() {
        int used = 0, idle = 0, down = 0;
        for(int i = 0; i < Manager.DISK_BLOCK; i++) {
            Node cell = blockGrid.getChildren().get(i);
            switch (Manager.FAT[i]) {
                case 0: cell.setStyle("-fx-border-color: #696969"); idle++; break;
                case -2: cell.setStyle("-fx-background-color: #FF4500;" +
                        "-fx-border-color: #696969"); down++; break;
                default: cell.setStyle("-fx-background-color: #00FF7F;" +
                        "-fx-border-color: #696969"); used++;
            }
            ((Label)((StackPane) FATContGrid.getChildren().get(i)).getChildren().get(0)).setText(String.valueOf(Manager.FAT[i]));
        }
        usedText.setText("使用: " + used);
        idleText.setText("空闲: " + idle);
        downText.setText("损坏: " + down);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for(int i = 0; i < Manager.DISK_BLOCK; i++) {
            int col = i % 8;
            int row = i / 8;
            blockGrid.add(new StackPane(new Label(String.valueOf(i))) , col, row);
            FATIndexGrid.add(new StackPane(new Label(String.valueOf(i))) , col, row);
            FATContGrid.add(new StackPane(new Label(String.valueOf(Manager.FAT[i]))) , col, row);
            FATContGrid.getChildren().get(i).setStyle("-fx-border-color: #696969");
        }
        update();
    }

}
