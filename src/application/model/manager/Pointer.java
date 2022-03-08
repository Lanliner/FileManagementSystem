package application.model.manager;

/**
 * 已打开文件表的读写指针
 */
public class Pointer {
    private int diskNum; //磁盘盘块号
    private int byteNum; //磁盘盘块内第几个字节
    public Pointer(int diskNum, int byteNum) {
        this.diskNum = diskNum;
        this.byteNum = byteNum;
    }
    public int getDiskNum() {return diskNum;}
    public int getByteNum() {return byteNum;}

    public void setDiskNum(int diskNum) {this.diskNum = diskNum;}
    public void setByteNum(int byteNum) {this.byteNum = byteNum;}
}
