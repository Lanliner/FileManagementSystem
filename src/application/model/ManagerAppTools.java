package application.model;

import application.model.manager.Entry;
import application.model.manager.Manager;
import application.model.manager.Tools;
import application.model.relic.ItemData;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeItem;

import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * 文件管理GUI操作
 */
public class ManagerAppTools {

    /**
     * 为目录树节点添加子节点
     */
    public static void addChildren(TreeItem<ItemData> item) {
        int index = item.getValue().getEntry().getStartNum();    //盘块索引
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            while(index >= 0) {
                for (int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {    //遍历当前盘块
                    // 跳到下个登记项
                    int pos = index * Manager.BLOCK_SIZE + i * 8;
                    raf.seek(pos);

                    //提取登记项信息
                    byte[] name = {raf.readByte(), raf.readByte(), raf.readByte()};
                    byte[] type = {raf.readByte(), raf.readByte()};
                    byte attribute = raf.readByte();
                    byte startNum = raf.readByte();
                    byte length = raf.readByte();

                    Entry entry = new Entry(name, type, attribute, startNum, length);
                    if(entry.isValid() && entry.isDirectory()) { //是目录登记项，添加入子节点
                        TreeItem<ItemData> newItem = new TreeItem<>(new ItemData(entry, pos));
                        item.getChildren().add(newItem);
                        newItem.setExpanded(true);
                    }
                }
                index = Manager.FAT[index]; //往下一盘块
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* 递归向子节点添加子节点 */
        for(TreeItem<ItemData> x : item.getChildren()) {
            addChildren(x);
        }
    }

    /**
     * 在目录树中打开内容为data的项
     * @param rootItem 根项
     * @param data 数据
     */
    public static void searchItem(TreeItem<ItemData> rootItem, ItemData data) {
        for(TreeItem<ItemData> item : rootItem.getChildren()) {
            if (item.getValue().equals(data)) {
                GUI.managerController.setCurItem(item);
                GUI.managerController.update();
                return;
            }
            //在子树中检索
            searchItem(item, data);
        }
    }

    /**
     * 返回当前目录节点的路径
     * @param curItem 当前目录
     * @return 路径字符串
     */
    public static String getPath(TreeItem<ItemData> curItem) {
        if(curItem.getParent() == null)
            return "根";
        return getPath(curItem.getParent()) + "/" + curItem.getValue();
    }

    /**
     * 根据当前目录项填充文件列表
     * @param fileList 文件列表
     * @param dirEntry 当前目录登记项
     */
    public static void fillList(ListView<ItemData> fileList, Entry dirEntry) {
        fileList.getItems().clear();   //清空原有数据
        int index = dirEntry.getStartNum();    //盘块索引
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            while(index > 0) {
                for (int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {    //遍历当前盘块
                    // 跳到下个登记项
                    int pos = index * Manager.BLOCK_SIZE + i * 8;
                    raf.seek(pos);

                    //提取登记项信息
                    byte[] name = { raf.readByte(), raf.readByte(), raf.readByte() };
                    byte[] type = { raf.readByte(), raf.readByte() };
                    byte attribute = raf.readByte();
                    byte startNum = raf.readByte();
                    byte length = raf.readByte();

                    Entry entry = new Entry(name, type, attribute, startNum, length);
                    if(entry.isValid())
                        fileList.getItems().add(new ItemData(entry, pos));
                }
                index = Manager.FAT[index]; //往下一盘块
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 给文件列表排序
     * @param fileList 文件列表
     */
    public static void sortList(ListView<ItemData> fileList) {
        ObservableList<ItemData> list = fileList.getItems();
        FXCollections.sort(list);
    }

    public static void createFile(String name, boolean systemFile) {
        /* 文件名和类型需合法 */
        String[] sect = name.split("\\.");
        if(sect.length != 2) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "文件名不合法", false);
            return;
        }
        if (sect[0].length() > 3) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "文件名过长，不得超过3字节", false);
            return;
        } else if (sect[1].length() > 2) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "类型名过长，不得超过2字节", false);
            return;
        }

        byte fileAttr = (byte) (systemFile ? 0b00000010 : 0b00000100);

        int result = Tools.createFile(name, fileAttr, GUI.managerController.getCurData().getEntry().getStartNum());
        if(result == 0) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录下存在同名文件", false);
            return;
        }
        if(result == -1) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "磁盘已满，无法创建文件", false);
            return;
        }
        if(result == -2) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录名存在非法字符", false);
            return;
        }
        if(result == 1) {   //创建成功
            GUI.managerController.update();
            GUI.usageController.update();
        }
    }

    public static void createDirectory(String name, boolean systemFile) {
        /* 目录名需合法 */
        if(name.contains(".")) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录名存在非法字符", false);
            return;
        }
        if (name.length() > 3) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录名过长，不得超过3字节", false);
            return;
        }

        byte fileAttr = (byte) (systemFile ? 0b00001010 : 0b00001100);

        int result = Tools.createDir(name, fileAttr, GUI.managerController.getCurData().getEntry().getStartNum());
        if(result == 0) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录下存在同名目录", false);
            return;
        }
        if(result == -1) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "磁盘已满，无法创建目录", false);
            return;
        }
        if(result == -2) {
            Util.showMessage(Alert.AlertType.WARNING, "创建失败", "目录名存在非法字符", false);
            return;
        }

        if(result == 1) {   //创建成功
            GUI.managerController.recreateTree();
            GUI.managerController.update();
            GUI.usageController.update();
        }
    }

    public static void deleteFile(TreeItem<ItemData> curItem, long posOfEntry) {
        int result;
        result = Tools.deleteFile(posOfEntry);
        if(result == 0) {
            Util.showMessage(Alert.AlertType.WARNING, "删除失败", "打开中的文件不能被删除", false);
            return;
        }

        GUI.managerController.recreateTree(curItem);
        GUI.managerController.update();
        GUI.usageController.update();
    }


    public static void deleteDir(TreeItem<ItemData> curItem, long posOfEntry) {
        int result;
        result = Tools.deleteDir(posOfEntry);
        if(result == 0)
            Util.showMessage(Alert.AlertType.WARNING, "删除出错", "待删除目录中有正在打开的文件，这些文件未被删除", false);

        GUI.managerController.recreateTree(curItem);
        GUI.managerController.update();
        GUI.usageController.update();
    }

    public static void openFile(TreeItem<ItemData> curItem, ItemData data) {
        int result;
        if(data.isReadOnly())
            result = Tools.openFile(data.getPos(), "r");
        else
            result = Tools.openFile(data.getPos(), "rw");

        if(result == 0) {
            GUI.managerController.showLimitText();
            return;
        }

        /* 提取文件内容 */
        StringBuilder content = new StringBuilder();
        byte[] buf = new byte[Manager.BLOCK_SIZE];
        int index = data.getEntry().getStartNum();
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            while(index > 0) {
                raf.seek((long) index * Manager.BLOCK_SIZE);
                Arrays.fill(buf, (byte) '\0');
                for(int i = 0; i < Manager.BLOCK_SIZE; i++) {
                    byte b = raf.readByte();
                    if(b == '#')    //到达文件尾
                        break;
                    buf[i] = b;
                }

                content.append(new String(buf));
                index = Manager.FAT[index]; //往下一盘块
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GUI.createEdit(curItem, data, content.toString(), data.isReadOnly());
    }

    public static void saveFile(TreeItem<ItemData> curItem, ItemData data, String content) {
        byte[] buf = content.getBytes();
        int index = data.getEntry().getStartNum();

        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            raf.seek((long) index * Manager.BLOCK_SIZE);    //往起始盘块
            for (byte b : buf) {
                raf.write(b);
                if (raf.getFilePointer() % Manager.BLOCK_SIZE == 0) {    //raf到达当前盘块尾
                    if (Manager.FAT[index] > 0) //还有后继盘块，往下个盘块
                        index = Manager.FAT[index];
                    else { //没有后继盘块，分配后继盘块
                        int lastIndex = index;
                        index = Tools.getIdleDiskNum();
                        Tools.updateFAT(raf, lastIndex, index);
                        Tools.updateFAT(raf, index, -1);
                    }
                    raf.seek((long) index * Manager.BLOCK_SIZE);
                }
            }
            raf.writeByte('#');
            if(Manager.FAT[index] > 0) {    //文件内容全部写回磁盘后仍有后继盘块，则需要回收盘块
                Tools.retrieveBlocks(raf, Manager.FAT[index]);
                Tools.updateFAT(raf, index, -1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        GUI.managerController.recreateTree(curItem);
        GUI.managerController.update();
        GUI.usageController.update();
    }

    public static void closeFile(ItemData data) {
        Tools.closeFile(data.getPos());
    }

    public static void renameFile(TreeItem<ItemData> curItem, long posOfEntry, String name) {
        int result;
        result = Tools.rename(posOfEntry, name);
        if(result == 0) {
            Util.showMessage(Alert.AlertType.WARNING, "重命名失败", "文件正在使用中", false);
            return;
        }
        if(result == -1) {
            Util.showMessage(Alert.AlertType.WARNING, "重命名失败", "名称不合法", false);
            return;
        }
        GUI.managerController.recreateTree(curItem);
        GUI.managerController.update();
        GUI.usageController.update();
    }

    public static void changeFile(long posOfEntry, boolean isDir, boolean systemFile, boolean readOnly) {
        byte fileAttr = 0;
        if(isDir)
            fileAttr += 0b00001000; //目录
        if(systemFile)
            fileAttr += 0b00000010; //系统
        else
            fileAttr += 0b00000100; //普通
        if(readOnly)
            fileAttr += 0b00000001; //只读
        Tools.changeAttribute(posOfEntry, fileAttr);
    }

}
