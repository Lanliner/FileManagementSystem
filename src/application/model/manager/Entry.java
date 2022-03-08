package application.model.manager;

import java.util.Arrays;

/**
 * 文件/目录登记项
 */
public class Entry {
    private byte[] name;        //文件名 / 目录名
    private byte[] type;        //文件类型名 / 未使用
    private byte attribute;     //文件属性 / 目录属性
    private byte startNum;         //文件起始盘块号 / 目录起始盘块号
    private byte diskBlockLength;         //文件长度(盘块数) / 目录长度

    public Entry(byte attribute, byte startNum) {
        this.name = null;
        this.type = null;
        this.attribute = attribute;
        this.startNum = startNum;
        this.diskBlockLength = 0;
    }

    /**
     * @param name      文件名
     * @param type      文件类型名
     * @param attribute 文件属性
     * @param startNum  文件起始盘块号
     * @param diskBlockLength   文件长度(盘块数)
     */
    public Entry(byte[] name, byte[] type, byte attribute, byte startNum, byte diskBlockLength) {
        this.name = name;
        this.type = type;
        this.attribute = attribute;
        this.startNum = startNum;
        this.diskBlockLength = diskBlockLength;
    }

    public byte[] getName() {
        return name;
    }

    public byte[] getType() {
        return type;
    }

    public byte getAttribute() {
        return attribute;
    }

    public byte getStartNum() {
        return startNum;
    }

    public byte getDiskBlockLength() {
        return diskBlockLength;
    }

    public boolean isDirectory() {
        return (attribute & 0x08) == 0x08;
    }

    public boolean isValid() {
        return name[0] != Manager.IDLE_ENTRY;
    }

    public boolean isReadOnly() {
        return (attribute & 0x01) == 0x01;
    }
}