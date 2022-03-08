package application.controller;

import application.model.AppearanceUtil;
import application.model.GUI;
import application.model.Util;
import application.model.manager.Manager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;

public class SystemController implements Initializable {

    /**********
     * 背景板  *
     **********/
    @FXML
    private AnchorPane baseAnchor;

    @FXML
    private ImageView backgroundView;

    /**********
     * 桌面应用 *
     **********/
    @FXML
    private BorderPane managerApp;
    @FXML
    private BorderPane helpApp;
    @FXML
    private BorderPane commandApp;
    @FXML
    private BorderPane aboutApp;

    /**
     * 初始化应用（图标）
     */
    private void initApp() {
        AppearanceUtil.setAppStyle(managerApp, helpApp, commandApp, aboutApp);
        managerApp.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2)
                managerAction(null);
        });
        helpApp.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                GUI.createHelp();
            }
        });
        commandApp.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                commandAction(null);
            }
        });
        aboutApp.setOnMouseClicked(event -> {
            if(event.getClickCount() == 2) {
                try {
                    GUI.createAbout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**********
     * 任务栏  *
     *********/
    @FXML
    private Button startMenuButton;
    private ContextMenu startMenu;

    @FXML
    void startMenuAction(MouseEvent event) {
        if(event.getButton().equals(MouseButton.PRIMARY))
            startMenu.show(startMenuButton, Side.TOP, 0, 0);
    }

    @FXML
    private Button managerButton;

    @FXML
    void managerAction(ActionEvent event) {
        try {
            GUI.createManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Button commandButton;

    @FXML
    void commandAction(ActionEvent event) {
        try {
            GUI.createCommand();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private Text timeText;
    @FXML
    private Text dateText;

    /**
     * 初始化任务栏
     */
    private void initTaskbar() {
        startMenuButton.setGraphic(new ImageView("/resource/win11.png"));
        managerButton.setGraphic(new ImageView("/resource/folder.png"));
        commandButton.setGraphic(new ImageView("/resource/command_mini.png"));
        AppearanceUtil.setButtonStyleBeta(startMenuButton, managerButton, commandButton);

        MenuItem shutdownButton = new MenuItem("  关闭系统  ");
        shutdownButton.setOnAction((event) -> {
            System.exit(0);
        });
        startMenu = new ContextMenu(shutdownButton);

        /* 初始化桌面时钟 */
        new Thread(() -> {
            while(true) {
                String[] args = Util.translateDate(new Date().getTime());
                timeText.setText(args[1]);
                dateText.setText(args[0]);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        backgroundView.fitWidthProperty().bind(baseAnchor.widthProperty());
        backgroundView.fitHeightProperty().bind(baseAnchor.heightProperty());
        backgroundView.setImage(new Image("/resource/background.png"));

        initApp();
        initTaskbar();

        Manager.getInstance().createDisk();
    }

}