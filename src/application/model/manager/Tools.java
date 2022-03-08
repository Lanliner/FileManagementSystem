package application.model.manager;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * 磁盘文件相关操作
 */
public class Tools {

    /**
     * 找空闲盘块
     * @return 空闲盘块的下标，-1表示没有可用的空闲盘块
     */
    public static int getIdleDiskNum() {
        int idleIndex;
        for(idleIndex = 3; idleIndex < 128; idleIndex++){
            if(Manager.FAT[idleIndex] == 0) {
                return idleIndex;
            }
        }
        return -1;
    }

    /**
     * 回收指定盘块及其后继盘块
     * @param startNum 回收的盘块链起始处
     */
    public static void retrieveBlocks(RandomAccessFile raf, int startNum) throws IOException{
        if(startNum < 0)
            return;
        long rafPointer = raf.getFilePointer();
        retrieveBlocks(raf, Manager.FAT[startNum]);
        updateFAT(raf, startNum, 0);
        raf.seek(rafPointer);
    }

    /**
     * 更新FAT数组和磁盘文件中的FAT表
     * @param raf
     * @param diskNum 要更新FAT表的盘块号
     * @param val 新的值
     */
    public static void updateFAT(RandomAccessFile raf, int diskNum, int val) throws IOException{
        long rafPointer = raf.getFilePointer();
        try {
            Manager.FAT[diskNum] = val;
            raf.seek(diskNum);
            raf.write(val);
        } catch (Exception e) {
            e.printStackTrace();
        }
        raf.seek(rafPointer);
    }

    /**
     *  得到指定文件或目录的各种信息，前提：没有打开磁盘文件流
     * @param posOfEntry
     * @return  [0]文件名， [1]文件类型， [2]文件属性， [3]起始盘块号， [4]最终盘块号， [5]总字节数， [6]总盘块数
     */
    public static String[] getFileInfo(long posOfEntry) {
        String[] fileInfo = new String[7];
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "r")) {
            raf.seek(posOfEntry);
            byte[] entry = new byte[Manager.DIR_ENTRY_NUM];
            raf.readFully(entry);

            String name = new String(new byte[] {entry[0], entry[1], entry[2]});
            String type = new String(new byte[] {entry[3], entry[4]});

            int attribute = entry[5];

            int startNum = entry[6];    // 记录起始盘块号
            int endNum = startNum;

            int diskBlockNum = startNum;
            int byteLength = 0;
            int diskBlockLength = 0;    // 初始化总盘块数

            if((attribute & 0x08) == 0x08) {    // 目录, 总字节数 = 每个盘块中的非空目录项占的字节数
                byte[] entry2 = new byte[8];

                // 遍历所有的磁盘块，记录总盘块数，记录非空目录项的数量，找出最后一个磁盘块
                while (diskBlockNum != -1) {
                    raf.seek((long) diskBlockNum * Manager.BLOCK_SIZE);
                    for(int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {    // 一个盘块内
                        raf.readFully(entry2);
                        if(entry2[0] != Manager.IDLE_ENTRY) {   // 非空目录项
                            byteLength += Manager.DIR_ENTRY_NUM;
                        }
                    }
                    diskBlockLength++;
                    endNum = diskBlockNum;      // 最后一个盘块号
                    diskBlockNum = Manager.FAT[diskBlockNum];
                }
            }
            else {    // 文件，总字节数 = (总盘块数 - 1) * 64 + 最后一个盘块的字节数

                // 遍历所有的磁盘块，记录总盘块数，找出最后一个磁盘块
                while (diskBlockNum != -1) {
                    diskBlockLength++;
                    endNum = diskBlockNum;      // 最后一个盘块号
                    diskBlockNum = Manager.FAT[diskBlockNum];
                }

                // 计算最后一个盘块的实际字节长度
                int lastDiskByteLength = 0;
                raf.seek((long) endNum * Manager.BLOCK_SIZE);
                for(int i = 0; i < Manager.BLOCK_SIZE; i++) {
                    // 读到文件结束符为止
                    if(raf.readByte() ==  Manager.EOF) {
                        break;
                    } else {
                        lastDiskByteLength ++;
                    }
                }
                byteLength = (diskBlockLength - 1) * Manager.BLOCK_SIZE + lastDiskByteLength;
            }

            fileInfo[0] = name;
            fileInfo[1] = type;
            fileInfo[2] = Integer.toString(attribute);
            fileInfo[3] = Integer.toString(startNum);
            fileInfo[4] = Integer.toString(endNum);
            fileInfo[5] = Integer.toString(byteLength);    // 总字节数
            fileInfo[6] = Integer.toString(diskBlockLength);  // 总盘块数
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileInfo;
    }

    /**
     * 查找父目录中是否存在这个文件
     * @param nameWithType  文件名（包含扩展名）
     * @param parStartNum   父目录的起始盘块号
     * @return  若文件存在，则返回在父目录中的目录项在磁盘文件中的位置， 否则返回-1
     */
    public static long getPosOfEntry(String nameWithType, int parStartNum) {
        int parDiskBlockNum = parStartNum;
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            byte[] entry = new byte[Manager.DIR_ENTRY_NUM];
            // 在父目录中查找该文件是否存在
            while (true) {
                raf.seek((long) parDiskBlockNum * Manager.BLOCK_SIZE);
                for (int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                    raf.readFully(entry);
                    String name = new String(new byte[]{entry[0], entry[1], entry[2]}) + "." + new String(new byte[]{entry[3], entry[4]});
                    // 文件存在
                    if (nameWithType.equals(name)) {
                        return raf.getFilePointer() - Manager.DIR_ENTRY_NUM;
                    }
                }
                // 找父目录的下一个磁盘块

                //遍历完最后一个盘块还没找到目标文件
                if(Manager.FAT[parDiskBlockNum] == -1) {
                    // 要打开的文件不存在
                    return -1;
                } else {
                    parDiskBlockNum = Manager.FAT[parDiskBlockNum];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

/*    *//**
     * 查找父目录中是否存在这个文件
     * @param fileName  文件名
     * @param parStartNum   父目录的起始盘块号
     * @return  若文件存在，则返回在父目录中的目录项entry， 否则返回null
     *//*
    public static byte[] isFileExist(String fileName, int parStartNum) {
        int parDiskBlockNum = parStartNum;
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            byte[] entry = new byte[8];
            // 在父目录中查找该文件是否存在
            while (true) {
                raf.seek((long) parDiskBlockNum * Manager.BLOCK_SIZE);
                for (int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                    raf.read(entry);
                    String name = new String(new byte[]{entry[0], entry[1], entry[2]}) + "." + new String(new byte[]{entry[3], entry[4]});
                    // 文件存在
                    if (name.equals(fileName)) {
                        return entry;
                    }
                }
                // 找父目录的下一个磁盘块

                //遍历完最后一个盘块还没找到目标文件
                if(Manager.FAT[parDiskBlockNum] == -1) {
                    // 要打开的文件不存在
                    return null;
                } else {
                    parDiskBlockNum = Manager.FAT[parDiskBlockNum];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /**
     *  未打开文件流
     * @param diskNum
     */
    public static void diskBlockDirInit(int diskNum) {
        try (RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")){
            raf.seek((long)diskNum * Manager.BLOCK_SIZE);
            for(int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                raf.write(Manager.IDLE_ENTRY);
                raf.skipBytes(Manager.DIR_ENTRY_NUM - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  已打开文件流
     * @param raf
     * @param diskNum
     */
    public static void diskBlockDirInit(RandomAccessFile raf, int diskNum) throws IOException {
        long rafPointer = raf.getFilePointer();
        try {
            raf.seek((long)diskNum * Manager.BLOCK_SIZE);
            for(int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                raf.write(Manager.IDLE_ENTRY);
                raf.skipBytes(Manager.DIR_ENTRY_NUM - 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        raf.seek(rafPointer);
    }

    /**
     *  删除文件目录项并归还文件所占磁盘空间
     */
    private static void release(RandomAccessFile raf, long posOfEntry) throws IOException{
        long rafPointer = raf.getFilePointer();

        // 删除文件目录项并归还文件所占磁盘空间
        try {
            raf.seek(posOfEntry);
            byte[] entry = new byte[8];
            raf.readFully(entry);
            int diskNum = entry[6];     // 记录待删除文件的起始盘块号

            raf.seek(posOfEntry);
            raf.writeByte(Manager.IDLE_ENTRY);  // 待删除文件的目录项设为空闲

            do{     // 归还文件所占磁盘空间，将其所有的FAT值设为0
                int nextDiskNum = Manager.FAT[diskNum];
                updateFAT(raf, diskNum, 0);
                diskNum = nextDiskNum;
            }while(diskNum != -1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        raf.seek(rafPointer);
    }

    /**
     *  删除文件目录项并归还文件所占磁盘空间
     */
    private static void release(long posOfEntry) {
        // 删除文件目录项并归还文件所占磁盘空间
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            raf.seek(posOfEntry);
            byte[] entry = new byte[8];
            raf.readFully(entry);
            int diskNum = entry[6];     // 记录待删除文件的起始盘块号

            raf.seek(posOfEntry);
            raf.writeByte(Manager.IDLE_ENTRY);  // 待删除文件的目录项设为空闲

            do{     // 归还文件所占磁盘空间，将其所有的FAT值设为0
                int nextDiskNum = Manager.FAT[diskNum];
                updateFAT(raf, diskNum, 0);
                diskNum = nextDiskNum;

            }while(diskNum != -1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *  检查文件名合法性，若合法则返回文件名和类型名
     * @param isDir     是文件还是目录
     * @param nameWithType  若是文件，则为“文件名.类型名” 若是目录，则就是目录名
     * @return  byte[][] 若输入非法文件名，则返回null， 若合法，则 [0] name, [1] type
     */
    private static byte[][] checkName(boolean isDir, String nameWithType) {
        byte[] name;
        byte[] type;

        if(isDir) {
            if(nameWithType.length() > 3) {
                return null;
            }
            if(nameWithType.contains("$") || nameWithType.contains(".") || nameWithType.contains("\\") || nameWithType.contains("/")) {
                return null;
            }
            name = nameWithType.getBytes();
            type = new byte[2];
        }
        else {
            String[] nwt = nameWithType.split("\\.");
            if(nwt[0].length() > 3 || nwt[1].length() > 2) {
                return null;
            }
            if(nwt[0].contains("$") || nwt[0].contains(".") || nwt[0].contains("\\") || nwt[0].contains("/")) {
                return null;
            }
            if(nwt[1].contains("$") || nwt[1].contains(".") || nwt[1].contains("\\") || nwt[1].contains("/")) {
                return null;
            }
            name = nwt[0].getBytes();
            type = nwt[1].getBytes();
        }

        byte[] targetName = {0, 0, 0};
        byte[] targetType = {0, 0};

        System.arraycopy(name, 0, targetName, 0, name.length);
        System.arraycopy(type, 0, targetType, 0, type.length);
        
        byte[][] bytes = new byte[2][];
        bytes[0] = targetName;
        bytes[1] = targetType;

        return bytes;
    }
    

    /**
     * 前提父目录已存在，将nameWithType分成两个合法的byte数组文件名和扩展名，遍历父目录已有的登记项，寻找第一个空闲的登记项。同时查看有无重名文件，如果有，则提示该文件已存在，建立文件失败；找到第一个空闲的登记项后，则为该
     * 文件建立文件目录，并分配给它一个磁盘块，最后填写目录。
     * @param nameWithType     要创建的文件的名字（含扩展名）
     * @param attribute     要创建的文件的属性
     * @param parStartNum   父目录的起始盘块号
     * @return  1 文件建立成功， 0 父目录下已存在同名文件，建立失败， -1 没有空闲磁盘块， -2 非法文件名（含扩展名）
     */
    public static int createFile(String nameWithType, byte attribute, int parStartNum) {

        boolean isDir = (attribute & 0x08) == 0x08;

        byte[][] bytes = checkName(isDir, nameWithType);
        if(bytes == null) {
            return -2;
        }
        byte[] bName = bytes[0];
        byte[] bType = bytes[1];


        // 当前的父目录的盘块号
        int parDirDiskNum = parStartNum;

        String targetName = new String(bName) + "." + new String(bType);
        byte[] entry = new byte[8];
        long idleEntryPos = 0;
        boolean hasIdleEntry = false;
        try(RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {
            // 已找到第一个空闲块,并且遍历完磁盘块,没有重名文件，才退出该循环
            while (true) {
                raf.seek( (long) parDirDiskNum * Manager.BLOCK_SIZE);
                for(int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                    raf.read(entry);
                    String entryName = new String(new byte[]{entry[0], entry[1], entry[2]}) + "." + new String(new byte[]{entry[3], entry[4]});
                    // 存在同名文件
                    if(entryName.equals(targetName)) {
                        return 0;
                    }
                    // 找到第一个空的目录登记项
                    if(!hasIdleEntry && entry[0] == Manager.IDLE_ENTRY) {
                        idleEntryPos = raf.getFilePointer() - Manager.DIR_ENTRY_NUM;
                        hasIdleEntry = true;
                    }
                }// 当前磁盘块的已经没有空闲的目录登记项，去找父目录的下一个磁盘块

                // 还没找到第一个空闲目录登记项，但已经遍历完最后一个盘块，则为父目录分配新的磁盘块
                if(!hasIdleEntry && Manager.FAT[parDirDiskNum] == -1) {
                    int idleDiskNum = getIdleDiskNum();
                    if(idleDiskNum == -1) {
                        return -1;      // 没有空闲的磁盘块了
                    }
                    // 原来的的最后一个盘块的FAT值更新为新的盘块
                    updateFAT(raf, parDirDiskNum, idleDiskNum);
                    // 新盘块的FAT值设为-1
                    updateFAT(raf, idleDiskNum, -1);
                    // 作为目录一部分的新盘块要初始化，每个目录项开头填上空目录标识符
                    diskBlockDirInit(raf, idleDiskNum);
                    parDirDiskNum = idleDiskNum;
                }
                // 已找到第一个空闲目录登记项，并且已经遍历完最后一个盘块
                else if(hasIdleEntry && Manager.FAT[parDirDiskNum] == -1) {
                    break;
                }
                // 还没有遍历完最后一个磁盘块 即FAT[parDirDiskNum] != -1
                else {
                    parDirDiskNum = Manager.FAT[parDirDiskNum];
                }
            }

            // 已找到父目录中第一个空闲目录登记项，并且已经遍历完最后一个盘块，没有同名文件
            // 找一个空闲的磁盘块
            int idleDiskNum = getIdleDiskNum();
            if(idleDiskNum == -1) {
                return -1;      // 没有空闲的磁盘块了
            }
            // 去到父目录中第一个空闲的目录登记项
            raf.seek(idleEntryPos);
            // 填写目录登记项: 文件名，文件类型，属性，起始盘块，文件长度（盘块数）
            raf.write(bName);   raf.write(bType);   raf.write(attribute);   raf.write(idleDiskNum);   raf.write(1);
            // 分配文件分配表
            updateFAT(raf, idleDiskNum, -1);
            // 在这个新文件中写入文件结束符
            raf.seek((long) idleDiskNum * Manager.BLOCK_SIZE);
            raf.writeByte(Manager.EOF);

            // 创建目录时，要初始化它的盘块
            if(isDir) {
               diskBlockDirInit(idleDiskNum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 创建成功
        return 1;
    }

    /**
     *  打开指定文件
     *
     *  前提文件已存在，且不能以写方式打开只读文件
     *  若文件已经打开(已存在于OpenFile表中)则不需要填写已打开文件表, 否则填写已打开文件表
     * @param posOfEntry  待打开文件的目录项在磁盘文件中的位置
     * @param operateType String类型的操作类型，只有 r w rw
     * @return  1 打开成功， 0 到达打开文件数量上限, 打开失败
     */
    public static int openFile(long posOfEntry, String operateType) {
        // 要打开的文件已存在于OpenFile表中，不再存入
        if(OpenFile.find(posOfEntry) != null) {
            return 1;
        }
        // 得到要打开文件的各种信息
        String[] fileInfo = getFileInfo(posOfEntry);

        byte attribute = (byte) Integer.parseInt(fileInfo[2]);
        int startNum = Integer.parseInt(fileInfo[3]);
        int endNum = Integer.parseInt(fileInfo[4]);
        int byteLength = Integer.parseInt(fileInfo[5]);

        // 建立OFTLE
        OFTLE oftle = new OFTLE(posOfEntry, attribute, startNum, endNum, byteLength, operateType);
        // 存入OpenFile表，此处该文件一定不在OpenFile表中
        int result = OpenFile.add(oftle, true);
        if(result == 1 || result == 0) {
            return 1;
        } else {
            // result == -1， 到达打开文件数量上限
            return 0;
        }
    }

    /**
     * @param name  目录的名字
     * @param attribute     目录属性
     * @param parStartNum   父目录的起始盘块号
     * @return  1 文件建立成功， 0 父目录下已存在同名文件，建立失败， -1 没有空闲磁盘块， -2 非法文件名（含扩展名）
     */
    public static int createDir(String name, byte attribute, int parStartNum) {
        // 调用createFile方法，参数nameWithType传入的是目录名，所以不能有"."
        if(name.contains(".")) {
            return -2;
        }
        return createFile(name, attribute, parStartNum);
    }

    /**
     *  关闭文件
     *
     *  看该文件是否打开，如果没有打开，就不用关闭；
     *  如果已经打开，最后从已打开文件表中删除对应项
     * @param posOfEntry 待关闭文件的目录项在磁盘文件的位置
     */
    public static void closeFile(long posOfEntry) {
        OFTLE oftle = OpenFile.find(posOfEntry);
        if(oftle == null) {
            return ;
        }
        OpenFile.remove(oftle);
    }

    /**
     *  删除绝对路径指定的文件
     *
     *  前提：文件的父目录存在，文件存在
     *
     * @param posOfEntry    待删除文件的目录项在磁盘文件中的位置
     * @return  1 删除成功， 0 文件打开中，不能删除
     */
    public static int deleteFile(long posOfEntry) {

        // 查找已打开文件表中是否存在该文件
        OFTLE oftle = OpenFile.find(posOfEntry);
        if(oftle != null) {     // 文件打开中，不能删除
            return 0;
        }
        // 删除文件目录项并归还文件所占磁盘空间
        release(posOfEntry);
        // 删除成功
        return 1;
    }

    /**
     * 删除绝对路径指定的文件
     * @param raf
     * @param posOfEntry
     * @return  1 删除成功， 0 文件打开中，不能删除
     */
    private static int deleteFile(RandomAccessFile raf, long posOfEntry) {
        // 查找已打开文件表中是否存在该文件
        OFTLE oftle = OpenFile.find(posOfEntry);
        if(oftle != null) {     // 文件打开中，不能删除
            return 0;
        }
        // 删除文件目录项并归还文件所占磁盘空间
        try{
            release(raf, posOfEntry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 删除成功
        return 1;
    }

    /**
     *  显示文件的各种信息
     * @param posOfEntry
     * @return  [0]文件名， [1]文件类型， [2]文件属性， [3]起始盘块号， [4]最终盘块号， [5]总字节数， [6]总盘块数
     */
    public static String[] typeFile(long posOfEntry) {
        return getFileInfo(posOfEntry);
    }

    /**
     *  改变文件的属性
     *
     *  检查文件是否打开，打开不能改变属性；没有打开，根据要求改变目录项中属性值。 前提: 父目录存在，文件存在
     * @param posOfEntry
     * @param newAttribute  新属性
     * @return  1 改变成功， 0 文件打开中，不能改变属性
     */
    public static int changeAttribute(long posOfEntry, byte newAttribute) {

        // 判断文件是否打开中
        OFTLE oftle = OpenFile.find(posOfEntry);
        if(oftle != null) {     // 文件打开中，不能修改
            return 0;
        }
        try (RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")){
            raf.seek(posOfEntry);
            byte[] entry = new byte[8];
            raf.readFully(entry);       // 读出整个目录项

            entry[5] = newAttribute;    // 变目录项中属性值

            raf.seek(posOfEntry);      // 写回整个目录项
            raf.write(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     *  改变文件名（包含扩展符）
     *
     *  检查文件是否打开，打开不能改变属性；没有打开，根据要求改变目录项中属性值。 前提: 父目录存在，文件存在
     * @param posOfEntry
     * @param nameWithType 新名字。若是文件，则为“文件名.类型名” 若是目录，则就是目录名
     * 
     * @return  1 改变成功， 0 文件打开中，不能重命名， -1 输入的文件名不合法
     */
    public static int rename(long posOfEntry, String nameWithType) {
        byte[] newName;
        byte[] newType;

        // 判断文件是否打开中
        OFTLE oftle = OpenFile.find(posOfEntry);
        if(oftle != null) {     // 文件打开中，不能修改
            return 0;
        }

        try (RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")){
            raf.seek(posOfEntry);
            byte[] entry = new byte[8];
            raf.readFully(entry);       // 读出整个目录项

            boolean isDir = (entry[5] & 0x08) == 0x08;

            byte[][] bytes = checkName(isDir, nameWithType);
            if(bytes != null) {
                newName = bytes[0];
                newType = bytes[1];

                entry[0] = newName[0];    // 改变变目录项中的文件名
                entry[1] = newName[1];
                entry[2] = newName[2];

                entry[3] = newType[0];
                entry[4] = newType[1];

                raf.seek(posOfEntry);      // 写回整个目录项
                raf.write(entry);
                return 1;
            }
            else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    /**
     *  对待删除目录中的每一个目录项：若为目录，则递归调用本方法，若为文件，则调用release方法
     *  递归结束条件，某个目录下没有子目录（都是文件目录项或空目录项）
     * @param raf
     * @param posOfEntry 要删除的目录的posOfEntry
     * @param startNum
     * @throws IOException
     * @return 1 删除成功， 0 目录中有文件正在打开中，不能删除
     */
    private static int deleteDir(RandomAccessFile raf, long posOfEntry, int startNum) throws IOException {
        long rafPointer = raf.getFilePointer();     // 保存旧现场

        byte[] entry = new byte[8];
        int diskNum = startNum;
        int result = 1;
        while(true) {
            raf.seek((long) diskNum * Manager.BLOCK_SIZE);  // 某一个盘块
            for(int i = 0; i < Manager.DIR_ENTRY_NUM; i++) {
                long posOfSubEntry = raf.getFilePointer();  // 子文件的posOfEntry
                raf.readFully(entry);
                if(entry[0] == Manager.IDLE_ENTRY) {    // 空目录项
                    continue;
                }
                byte attribute = entry[5];
                if((attribute & 0x08) == 0x08) {    // 子目录
                    int subDirStartNum = entry[6];
                    result *= deleteDir(raf, posOfSubEntry, subDirStartNum);        // 删除该子目录

                } else {    // 子文件
                    result *= deleteFile(raf, posOfSubEntry);
                }
            }
            if(Manager.FAT[diskNum] == -1) {    // 已读完最后一个盘块
                break;
            } else {    // 下一个盘块
                diskNum = Manager.FAT[diskNum];
            }
        }

        if(result == 1) {   // 所有子文件都删除成功
            release(raf, posOfEntry);   // 释放该子目录的所有的磁盘块
        }

        raf.seek(rafPointer);   // 恢复旧现场
        return result;
    }

    /**
     *  删除目录，清空该目录下的所有目录项entry，删除这个目录下的所有子目录和子文件，释放它们占有的磁盘空间
     *  若为根目录，则不删除
     *  前提：待删除的目录一定存在
     * @param posOfEntry    待删除目录在其父目录中的目录项的位置
     * @return  1 删除成功， 0 目录中有文件正在打开中，不能删除， -1 为根目录，不能删除
     */
    public static int deleteDir(long posOfEntry) {
        if(posOfEntry == 0) {   // 为根目录
            return -1;
        }
        int result = 1;
        byte[] entry = new byte[8];
        try (RandomAccessFile raf = new RandomAccessFile(Manager.FILE_NAME, "rw")) {

            raf.seek(posOfEntry);
            raf.readFully(entry);

            int startNum = entry[6];
            result = deleteDir(raf, posOfEntry, startNum);  // 删除这个指定目录

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
