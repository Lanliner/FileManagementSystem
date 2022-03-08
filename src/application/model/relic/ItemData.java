package application.model.relic;

import application.model.manager.Entry;

public class ItemData implements Comparable<ItemData> {

    private final Entry entry;    //登记项
    private final long pos;    //登记项在磁盘文件中的位置, 为0时视为根目录

    public ItemData() {  //无参构造方法用于构造根目录项
        entry = new Entry((byte)0x08, (byte)2);
        pos = 0;
    }

    public ItemData(Entry entry, int pos) {
        this.entry = entry;
        this.pos = pos;
    }

    public Entry getEntry() {
        return entry;
    }

    public long getPos() {
        return pos;
    }

    public boolean isDirectory() {
        return entry.isDirectory();
    }

    public boolean isReadOnly() { return entry.isReadOnly(); }

    public boolean equals(ItemData id) {
        return pos == id.pos;
    }

    @Override
    public int compareTo(ItemData id) {
        if(isDirectory() && !id.isDirectory())
            return -1;
        if(!isDirectory() && id.isDirectory())
            return +1;
        return toString().compareTo(id.toString());
    }

    @Override
    public String toString() {
        if (pos == 0) {
            return "根目录";
        }
        String name = new String(entry.getName()).split("\\u0000")[0];  //截去NUL
        if(isDirectory()) {
            return name;
        }
        String type = new String(entry.getType()).split("\\u0000")[0];
        return name + "." + type;
    }
}
