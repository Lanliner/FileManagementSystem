package application.model.manager;

/**
 * 已打开文件表项
 */
public class OFTLE {
    private long posOfEntry;
    private String absolutePath;    //文件绝对路径名
    private byte attribute;     //文件的属性
    private int startNum;       //文件起始盘块号
    private int byteLength;         //文件长度，文件占用的总字节数(不考虑文件中的空闲)
    private byte operateType;    //操作类型，用“0”表示以读操作方式打开文件，用“1”表示以写操作方式打开文件，用“2”表示以读写操作方式打开文件
    private Pointer read;       //读文件的位置，文件打开时 diskNum 为文件起始盘块号，byteNum 为“0”
    private Pointer write;      //写文件的位置，打开文件时 diskNum 和 byteNum 为文件的末尾位置

    public OFTLE(long posOfEntry, byte attribute, int startNum, int endNum, int byteLength, String operateType) {
        this.posOfEntry = posOfEntry;
        this.attribute = attribute;
        this.startNum = startNum;
        this.byteLength = byteLength;
        switch (operateType) {
            case "r": this.operateType = 0; break;
            case "w": this.operateType = 1; break;
            case "rw": this.operateType = 2; break;
            default: this.operateType = 2;
        }
        read = new Pointer(startNum, 0);
        write = new Pointer(endNum, byteLength % Manager.BLOCK_SIZE);
    }
    public long getPosOfEntry() {return posOfEntry;}
    public String getAbsolutePath() {
        return absolutePath;
    }
    public byte getOperateType() {return operateType;}
    public Pointer getRead() {return read;}
    public Pointer getWrite() {return write;}
}
