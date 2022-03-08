package application.controller;

import application.model.GUI;
import application.model.ManagerAppTools;
import application.model.manager.Manager;
import application.model.relic.FileListCell;
import application.model.relic.ItemData;
import application.model.relic.DirTreeCell;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

import static java.lang.Thread.sleep;

public class ManagerController implements Initializable {

    private static Stage stage = null;

    public static Stage getStage() {
        return stage;
    }

    public static void setStage(Stage stage) {
        ManagerController.stage = stage;
    }


    /*********
     * 目录树 *
     *********/

    private TreeItem<ItemData> curItem; //当前目录

    @FXML
    private AnchorPane limitPane;

    public void setCurItem(TreeItem<ItemData> curItem) {
        this.curItem = curItem;
    }

    public ItemData getCurData() {
        if(curItem == null)
            return null;
        return curItem.getValue();
    }

    @FXML
    private TreeView<ItemData> dirTree;

    /**
     * 初始化目录树
     */
    private void initTree() {
        dirTree.setCellFactory(param -> new DirTreeCell());
        dirTree.setOnMouseClicked(event -> {
            curItem = dirTree.getSelectionModel().getSelectedItem();
            update();
        });

        TreeItem<ItemData> rootItem = new TreeItem<>(new ItemData());
        dirTree.setRoot(rootItem);
        curItem = dirTree.getRoot();
        recreateTree();
    }

    /**
     * 重构目录树或指定子树
     */
    public void recreateTree() {
        dirTree.getRoot().getChildren().clear();
        ManagerAppTools.addChildren(dirTree.getRoot());
        dirTree.getRoot().setExpanded(true);
    }
    public void recreateTree(TreeItem<ItemData> item) {
        item.getChildren().clear();
        ManagerAppTools.addChildren(item);
        item.setExpanded(true);
    }


    /**********
     * 文件列表 *
     **********/
    @FXML
    private TextField pathText;

    @FXML
    private ListView<ItemData> fileList;


    /**
     * 初始化文件列表
     */
    private void initList() {
        fileList.setCellFactory(param -> new FileListCell());

        /* 默认右键菜单 */
        MenuItem createFile = new MenuItem("创建文件");
        MenuItem createDir = new MenuItem("创建文件夹");
        createFile.setOnAction(event -> {
            createFileAction();
        });
        createDir.setOnAction(event -> {
            createDirAction();
        });
        fileList.setContextMenu(new ContextMenu(createFile, createDir));
    }

    /**
     * 显示已达上限
     */
    public void showLimitText() {
        new Thread(() -> {
            Platform.runLater(() -> limitPane.setVisible(true));
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(() -> limitPane.setVisible(false));
        }).start();
    }


    /**********
     * 底部按钮 *
     **********/

    @FXML
    private Button usageButton;

    @FXML
    private Button crippleButton;

    @FXML
    private Button fixButton;

    @FXML
    private Button formatButton;

    @FXML
    void usageAction(ActionEvent event) throws Exception {
        GUI.createUsage();
    }

    @FXML
    void crippleAction(ActionEvent event) {
        Manager.getInstance().crippleBlock();
        update();
        GUI.usageController.update();
    }

    @FXML
    void fixAction(ActionEvent event) {
        Manager.getInstance().fixDisk();
        update();
        GUI.usageController.update();
    }

    @FXML
    void formatAction(ActionEvent event) {
        Manager.getInstance().formatDisk();
        update();
        GUI.usageController.update();
    }


    /**********
     * 右键菜单 *
     **********/
    public void openAction() {
        ItemData data = fileList.getSelectionModel().getSelectedItem();
        if(data == null)
            return;

        if(data.getEntry().isDirectory()) //打开目录
            ManagerAppTools.searchItem(dirTree.getRoot(), data);
        else //打开文件
            ManagerAppTools.openFile(curItem, data);
    }

    public void deleteAction() {
        ItemData data = fileList.getSelectionModel().getSelectedItem();
        if(data == null)
            return;
        if(data.isDirectory())
            ManagerAppTools.deleteDir(curItem, data.getPos());
        else
            ManagerAppTools.deleteFile(curItem, data.getPos());
    }

    public void changeAction() {
        GUI.createControl(curItem, fileList.getSelectionModel().getSelectedItem(), 3);
    }

    public void createFileAction() {
        GUI.createControl(curItem, null, 1);
    }

    public void createDirAction() {
        GUI.createControl(curItem, null, 2);
    }

    public void attributeAction() {
        GUI.createAttribute(pathText.getText(), fileList.getSelectionModel().getSelectedItem());
    }


    /**
     * 更新界面
     */
    public void update() {
        if(curItem == null)
            return;

        /* 更新路径 */
        String path = ManagerAppTools.getPath(curItem);
        pathText.setText(path);
        /* 更新文件列表 */
        ManagerAppTools.fillList(fileList, curItem.getValue().getEntry());
        ManagerAppTools.sortList(fileList);

        dirTree.getSelectionModel().select(curItem);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        /* 初始化使用情况窗口 */
        try {
            GUI.createUsage();
        } catch (Exception e) {
            e.printStackTrace();
        }
        UsageController.getStage().hide();

        /* 初始化目录树 */
        initTree();

        /* 初始化文件列表 */
        initList();

        /* 初始化路径框 */
        pathText.setText("根目录/");

        update();
    }

}
