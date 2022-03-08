package application.model.relic;

import javafx.scene.control.TreeCell;
import javafx.scene.image.ImageView;

public class DirTreeCell extends TreeCell<ItemData> {
    @Override
    protected void updateItem(ItemData item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            this.setGraphic(null);
            this.setText("");
        } else if (item.isDirectory()) {
            this.setGraphic(new ImageView("/resource/dir.png"));
            this.setText(item.toString());
        } else {
            this.setGraphic(null);
            this.setText(item.toString());
        }
    }
}
