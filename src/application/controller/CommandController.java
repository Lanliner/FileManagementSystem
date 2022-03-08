package application.controller;

import application.model.CommandAppTools;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CommandController {

    private static Stage stage = null;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        CommandController.stage = stage;
    }

    @FXML
    private TextField inputText;

    @FXML
    private TextArea outputText;

    public void clearInput() {
        inputText.clear();
    }

    /**
     *  添加连续的输出，没有回车结尾
     */
    public void addSuccessiveOutput(String s) {
        outputText.setText(outputText.getText() + s);
        outputText.setScrollTop(Double.MAX_VALUE);
    }

    public void addOutput(String s) {
        outputText.setText(outputText.getText() + s + "\n");
        outputText.setScrollTop(Double.MAX_VALUE);
    }

    public void clearOutput() {
        outputText.clear();
    }

    @FXML
    void inputAction(ActionEvent event) {
        String input = inputText.getText().trim();
        if(input.equals(""))
            return;

        String[] sec = input.split("\\s+");
        CommandAppTools.executeCommand(this, sec);
    }

}

