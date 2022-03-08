package application.model;

import application.controller.CommandController;
import application.controller.ManagerController;
import application.controller.UsageController;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.text.SimpleDateFormat;

public class Util {

    /**
     * 清空多个文本框
     * @param inputControls 文本框
     */
    public static void clearTextField(TextInputControl... inputControls) {
        for (TextInputControl inputControl : inputControls) {
            inputControl.clear();
        }
    }

    /**
     * 设置节点锚定
     * @param node 节点
     * @param top 顶间距
     * @param bottom 底间距
     * @param left 左间距
     * @param right 右间距
     */
    public static void setAnchor(Node node, Double top, Double bottom, Double left, Double right) {
        if(top != null) {
            AnchorPane.setTopAnchor(node, top);
        }
        if(bottom != null) {
            AnchorPane.setBottomAnchor(node, bottom);
        }
        if(left != null) {
            AnchorPane.setLeftAnchor(node, left);
        }
        if(right != null) {
            AnchorPane.setRightAnchor(node, right);
        }
    }

    /**
     * 将long表示的大小转换为字符串
     * @param length 以long表示的日期
     * @return 转换得到的字符串，单位不定
     */
    public static String translateSize(long length) {
        if(length == 0) {
            return "0B";
        }
        double sizeKB = (double)length / 1024;
        if(sizeKB >= 1024) {
            return String.format("%.2f MB", sizeKB / 1024);
        } else {
            return String.format("%.0f KB", sizeKB);
        }
    }

    /**
     * 将long表示的日期转换为字符串
     * @param date 以long表示的日期
     * @return 转换得到的字符串数组，格式"yyyy/MM/dd"、"HH:MM"
     */
    public static String[] translateDate(long date) {
        SimpleDateFormat dateDF = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat timeDF = new SimpleDateFormat("HH:mm");
        return new String[] {dateDF.format(date), timeDF.format(date)};
    }

    /**
     * 弹出警报窗口
     * @param alertType 警报类型
     * @param title 警报标题
     * @param content 警报内容
     * @param beep 是否发声
     */
    public static void showMessage(Alert.AlertType alertType, String title, String content, boolean beep) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        if(beep) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }

        Platform.runLater(() -> {
            //其它窗口置顶优先度后移
            setAllTop(false, CommandController.getStage(), ManagerController.getStage(), UsageController.getStage());

            alert.showAndWait();

            //恢复其它窗口置顶
            setAllTop(true, CommandController.getStage(), ManagerController.getStage(), UsageController.getStage());
        });
    }

    /**
     * 批量设置窗口置顶属性
     * @param value 是否置顶
     * @param stages 舞台
     */
    public static void setAllTop(boolean value, Stage... stages) {
        for(Stage stage : stages) {
            if(stage != null)
                stage.setAlwaysOnTop(value);
        }
    }

}
