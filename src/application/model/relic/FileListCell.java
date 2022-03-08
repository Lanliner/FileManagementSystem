package application.model.relic;

import application.model.GUI;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;

public class FileListCell extends ListCell<ItemData> {
    private final ContextMenu contextMenu = new ContextMenu();
    private final ContextMenu emptyContextMenu = new ContextMenu();

    public FileListCell() {
        MenuItem open = new MenuItem("打开");
        MenuItem delete = new MenuItem("删除");
        MenuItem change = new MenuItem("重命名/属性修改");
        MenuItem attibute = new MenuItem("显示属性");
        open.setOnAction(event -> {
            GUI.managerController.openAction();
        });
        delete.setOnAction(event -> {
            GUI.managerController.deleteAction();
        });
        change.setOnAction(event -> {
            GUI.managerController.changeAction();
        });
        attibute.setOnAction(event -> {
            GUI.managerController.attributeAction();
        });
        contextMenu.getItems().addAll(open, delete, change, attibute);

        MenuItem createFile = new MenuItem("创建文件");
        MenuItem createDir = new MenuItem("创建文件夹");
        createFile.setOnAction(event -> {
            GUI.managerController.createFileAction();
        });
        createDir.setOnAction(event -> {
            GUI.managerController.createDirAction();
        });
        emptyContextMenu.getItems().addAll(createFile, createDir);
    }

    @Override
    protected void updateItem(ItemData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
            this.setText("");
            setContextMenu(emptyContextMenu);
        } else {
            setOnMouseClicked(event -> {
                if(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                    GUI.managerController.openAction();
                }
            });
            setContextMenu(contextMenu);
            if (item.isDirectory())
                this.setGraphic(new ImageView("/resource/dir.png"));
            else
                this.setGraphic(new ImageView("/resource/file.png"));
            this.setText(item.toString());
        }
    }

}
