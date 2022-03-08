package application.model;

import application.controller.CommandController;
import application.controller.ManagerController;
import application.controller.UsageController;

import application.model.manager.Tools;
import application.model.relic.ItemData;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;


public class GUI {

    public static ManagerController managerController;
    public static UsageController usageController;

    public static void createSystem(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("/application/view/system.fxml"));
        Parent root = loader.load();

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("系统");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setMaximized(true);

        stage.show();
    }

    public static void createHelp() {
        Stage stage = new Stage(StageStyle.UTILITY);

        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(400, 100);
        Text text = new Text("获取帮助\n请添加QQ：1427341069");
        text.setFont(Font.font(null, 24));
        text.setWrappingWidth(360);

        ap.getChildren().add(text);
        Util.setAnchor(text, 20.0, null, 20.0, 20.0);


        Scene scene = new Scene(ap);
        stage.setTitle("帮助文档");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        stage.show();
    }

    public static void createAbout() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("/application/view/about.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle("关于我们");
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        stage.show();
    }

    public static void createCommand() throws Exception {
        Stage stage = CommandController.getStage();
        if(stage != null) {
            stage.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("/application/view/command.fxml"));
        Parent root = loader.load();

        stage = new Stage();
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("命令行");
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        CommandController.setStage(stage);

        stage.show();
    }

    public static void createManager() throws Exception {
        Stage stage = ManagerController.getStage();
        if(stage != null) {
            stage.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("/application/view/manager.fxml"));
        Parent root = loader.load();

        stage = new Stage();
        stage.setTitle("文件管理器");
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        ManagerController.setStage(stage);
        managerController = loader.getController();

        stage.show();
    }

    public static void createUsage() throws Exception {
        Stage stage = UsageController.getStage();
        if(stage != null) {
            stage.show();
            return;
        }

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(GUI.class.getResource("/application/view/usage.fxml"));
        Parent root = loader.load();

        stage = new Stage();
        stage.setTitle("磁盘使用情况");
        stage.initStyle(StageStyle.UTILITY);
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        UsageController.setStage(stage);
        usageController = loader.getController();

        stage.show();
    }

    /**
     * 创建列表项控制窗口
     * @param data 数据。若为创建操作，此项为null。
     * @param type 类型：1.创建文件  2.创建目录  3.文件重命名和属性修改
     */
    public static void createControl(TreeItem<ItemData> curItem, ItemData data, int type) {
        Stage stage = new Stage(StageStyle.UTILITY);

        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(400, 120);
        Label l = new Label("名称：");
        TextField tf = new TextField();
        Button ok = new Button("确定");
        RadioButton ro = new RadioButton("只读");
        RadioButton sf = new RadioButton("系统文件");
        ap.getChildren().addAll(l, tf, ok, ro, sf);
        Util.setAnchor(l, 10.0, null, 10.0, null);
        Util.setAnchor(ro, 10.0, null, 220.0, null);
        Util.setAnchor(sf, 10.0, null, 300.0, null);
        Util.setAnchor(tf, 40.0, null, 10.0, 10.0);
        Util.setAnchor(ok, null, 10.0, null, 10.0);

        switch (type) {
            case 1:
                stage.setTitle("创建文件");
                ro.setVisible(false);
                ok.setOnAction(event -> {
                    String name = tf.getText().trim();
                    if(name.equals(""))
                        return;
                    stage.close();
                    ManagerAppTools.createFile(name, sf.isSelected());
                });
                break;
            case 2:
                stage.setTitle("创建文件夹");
                ro.setVisible(false);
                ok.setOnAction(event -> {
                    String name = tf.getText().trim();
                    if(name.equals(""))
                        return;
                    stage.close();
                    ManagerAppTools.createDirectory(name, sf.isSelected());
                });
                break;
            case 3:
                stage.setTitle("重命名/属性修改");
                ok.setOnAction(event -> {
                    String name = tf.getText().trim();
                    if(name.equals(""))
                        return;
                    stage.close();
                    ManagerAppTools.changeFile(data.getPos(), data.isDirectory(), sf.isSelected(), ro.isSelected());
                    ManagerAppTools.renameFile(curItem, data.getPos(), name);
                });
                break;
        }

        Scene scene = new Scene(ap);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        stage.showAndWait();
    }

    /**
     * 显示文件属性
     * @param path 路径
     * @param data 数据
     */
    public static void createAttribute(String path, ItemData data) {
        Stage stage = new Stage(StageStyle.UTILITY);

        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(400, 160);
        Label pathLabel = new Label("文件路径名：");
        Text pathText = new Text(path + "/" + data);
        Label lengthLabel = new Label("文件大小：");
        Text lengthText = new Text(Tools.getFileInfo(data.getPos())[5] + "字节");
        Label typeLabel = new Label("文件类型：");
        String s = "";
        if((data.getEntry().getAttribute() & 0b00000010) == 0b00000010)
            s += "系统文件  ";
        else
            s += "普通文件  ";
        if((data.getEntry().getAttribute() & 0b00000001) == 0b00000001)
            s += "只读文件";
        Text typeText = new Text(s);

        ap.getChildren().addAll(pathLabel, pathText, typeLabel, typeText, lengthLabel, lengthText);
        Util.setAnchor(pathLabel, 10.0, null, 10.0, null);
        Util.setAnchor(pathText, 30.0, null, 20.0, 10.0);
        Util.setAnchor(lengthLabel, 60.0, null, 10.0, null);
        Util.setAnchor(lengthText, 80.0, null, 20.0, null);
        Util.setAnchor(typeLabel, 110.0, null, 10.0, null);
        Util.setAnchor(typeText, 130.0, null, 20.0, null);


        Scene scene = new Scene(ap);
        stage.setTitle(data + "属性");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        stage.showAndWait();
    }

    /**
     * 打开文件编辑界面
     * @param data 数据
     */
    public static void createEdit(TreeItem<ItemData> dirItem, ItemData data, String content, boolean readOnly) {
        Stage stage = new Stage(StageStyle.UTILITY);

        AnchorPane ap = new AnchorPane();
        ap.setPrefSize(600, 400);
        TextArea editArea = new TextArea(content);
        editArea.setWrapText(true);
        Button save = new Button("保存");
        Button cancel = new Button("取消");

        ap.getChildren().addAll(editArea, save, cancel);
        Util.setAnchor(editArea, 10.0, 60.0, 10.0, 10.0);
        Util.setAnchor(save, null, 16.0, null, 80.0);
        Util.setAnchor(cancel, null, 16.0, null, 20.0);

        if(readOnly) {
            editArea.setEditable(false);
            save.setDisable(true);
        }

        save.setOnAction(event -> ManagerAppTools.saveFile(dirItem, data, editArea.getText()));

        cancel.setOnAction(event -> stage.close());

        stage.setOnHidden(event -> ManagerAppTools.closeFile(data));


        Scene scene = new Scene(ap);
        String title = data + "编辑";
        if(readOnly)
            title += " (只读)";
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);

        stage.showAndWait();
    }

}
