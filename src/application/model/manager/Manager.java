package application.model.manager;

import application.model.Util;
import javafx.scene.control.Alert;

import java.io.*;
import java.util.Random;

/**
 * 文件管理器
 */
public class Manager {

    private static Manager manager = null;

    public static final int DISK_BLOCK = 128;
    public static final int BLOCK_SIZE = 64;
    public static final int DIR_ENTRY_NUM = 8;
    public static final byte EOF = (byte)'#';  //文件尾空标志
    public static final byte IDLE_ENTRY = (byte)'$';  //目录项空标志
    public static final int DISK_SIZE = BLOCK_SIZE * DISK_BLOCK;
    public static final String FILE_NAME = "virtual disk.dat";


    public static int[] FAT = new int[128];

    private Manager() {}

    public static Manager getInstance() {
        if(manager == null) {
            manager = new Manager();
        }
        return manager;
    }

    /**
     * 输出磁盘内容到控制台
     */
    public void printDisk() {
        try(FileInputStream fis = new FileInputStream(FILE_NAME)) {
            for(int i = 0; i < DISK_SIZE; i++) {
                System.out.print(fis.read() + "\t");
                if ((i + 1) % 32 == 0)
                    System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建虚拟磁盘文件并初始化
     */
    public void createDisk() {
        File f = new File(FILE_NAME);
        //已存在磁盘文件，读取FAT后返回
        if(f.exists()) {
            try(FileInputStream fis = new FileInputStream(FILE_NAME)) {
                /* 内存中的FAT */
                for(int i = 0; i < DISK_BLOCK; i++) {
                    FAT[i] = (byte)fis.read();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        /* 创建磁盘文件并初始化 */
        Util.showMessage(Alert.AlertType.INFORMATION, "未发现磁盘数据", "将新建空磁盘", false);
        formatDisk();
    }

    /**
     * 格式化磁盘
     */
    public void formatDisk() {
        try(FileOutputStream fos = new FileOutputStream(FILE_NAME)) {
            /* 创建文件分配表 */
            fos.write(-1);     FAT[0] = -1;
            fos.write(-1);     FAT[1] = -1;
            fos.write(-1);     FAT[2] = -1;
            for(int i = 3; i < DISK_BLOCK; i++) {
                fos.write(0);
                FAT[i] = 0;
            }
            /* 初始化根目录 */
            for(int i = DISK_BLOCK; i < DISK_BLOCK + BLOCK_SIZE; i++) {
                if(i % 8 == 0)
                    fos.write(IDLE_ENTRY);
                else
                    fos.write(0);
            }
            /* 初始化其余磁盘存储 */
            for(int i = DISK_BLOCK + BLOCK_SIZE; i < DISK_SIZE; i++) {
                fos.write(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 随机损坏一个非系统区磁盘块
     */
    public void crippleBlock() {
        try(RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "rw")) {
            int index = -1;
            Random rand = new Random();
            do {
                index = 3 + rand.nextInt(DISK_BLOCK - 3);
                raf.seek(index);
            } while(raf.readByte() == (byte)-2); //跳过已损坏磁盘块
            raf.seek(index);
            raf.writeByte(-2);
            Manager.FAT[index] = -2;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修复所有损坏的非系统区磁盘块
     */
    public void fixDisk() {
        try(RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "rw")) {
            for (int i = 0; i < DISK_BLOCK; i++) {
                if (raf.readByte() == -2) {
                    raf.seek(i);
                    raf.writeByte(0);
                    Manager.FAT[i] = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
