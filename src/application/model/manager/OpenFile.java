package application.model.manager;

import java.util.ArrayList;

/**
 * 已打开文件表
 */
public class OpenFile {
    private static int quantity = 5;
    private static final ArrayList<OFTLE> oftles = new ArrayList<>();

    /**
     * @param newOftle
     * @return 1 成功添加到OpenFile表,  0 该oftle已存在于这个OpenFile表，  -1  已达到打开文件上限
     */
    public static int add(OFTLE newOftle) {
        if(oftles.size() == quantity) {
            return -1;
        }
        else if(find(newOftle.getPosOfEntry()) == null) {
            oftles.add(newOftle);
            return 1;
        }
        else {
            return 0;
        }
    }

    /** 谨慎使用！！！
     *  notExist为true, 则会直接存入OpenFile表
     * @param notExist  若为true，则确认要存入的oftle不存在于OpenFile表
     * @return  1 成功添加到OpenFile表,  0 该oftle已存在于这个OpenFile表，  -1  已达到打开文件上限
     */
    public static int add(OFTLE newOftle, boolean notExist) {
        if(oftles.size() == quantity) {
            return -1;
        }
        if(notExist || find(newOftle.getPosOfEntry()) == null) {
            oftles.add(newOftle);
            return 1;
        }
        else {
            return 0;
        }
    }

    public static void remove(OFTLE newOftle) {
        oftles.remove(newOftle);
    }


    /**
     * @param posOfEntry 要找的文件的绝对路径
     * @return  在OpenFile表中找到的该绝对路径对应的oftle，找不到则返回null
     */
    public static OFTLE find(long posOfEntry) {
        for(OFTLE oftle: oftles) {
            // 已存在该oftle
            if(oftle.getPosOfEntry() == posOfEntry) {
                return oftle;
            }
        }
        return null;
    }

    public static ArrayList<OFTLE> getOFTLES() {
        return oftles;
    }
    public static int getLength() {
        return oftles.size();
    }
}
