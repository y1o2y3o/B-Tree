package com.azure.disk.bptree01;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class FileMapper {
    static final long NIL = -1L;
    public static final int _256B = 256;
    public static final int _512B = 512;
    public static final int _1KB = 1024;
    public static final int fileHeaderCapacity = 64;
    static final HashMap<String, Long> posMap = new HashMap<>();

    static {
        posMap.put("pageSize", 0L);
        posMap.put("keySize", 4L);
        posMap.put("ptrSize", 8L);
        posMap.put("capacity", 12L);
        posMap.put("usedPages", 20L);
        posMap.put("root", 28L);
        posMap.put("sqt", 36L);
        posMap.put("firstFree", 44L);
    }

    int m; // m阶B树
    private int pageSize;
    private int keySize; // 4B
    private int ptrSize; // 4B
    public long capacity;
    public long usedPages;
    long root;
    long sqt;
    private long firstFree;
    private String fileName;
    private RandomAccessFile file;

    private FileMapper() {
    }

    // 获取file
    private void initFile() {
        if (file == null) {
            try {
                file = new RandomAccessFile(fileName, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // 设置新的根结点
    public void setNewRoot(long root) {
        initFile();
        try {
            file.seek(posMap.get("root"));
            file.writeLong(root);
        } catch (IOException ex) {
            ex.printStackTrace();

        }
    }

    private long getPagePosition(long pageIndex) {
        return pageIndex * pageSize + fileHeaderCapacity;
    }

    private void doubleCapacity() {
        initFile();
        try {
            long oldCapacity = capacity;
            capacity <<= 1;
            file.seek(getPagePosition(oldCapacity));
            byte[] page = new byte[pageSize - 8];
            for (long i = oldCapacity; i < capacity; ++i) {
                file.writeLong(i + 1);
                file.write(page);
            }
            file.seek(posMap.get("firstFree"));
            long nextPage = file.readLong();
            file.seek(posMap.get("firstFree"));
            file.writeLong(oldCapacity);
            firstFree = oldCapacity;
            file.seek(getPagePosition(capacity - 1));
            file.writeLong(nextPage);
            file.seek(posMap.get("capacity"));
            file.writeLong(capacity);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public long allocatePage() {
        if (usedPages == capacity) {
            doubleCapacity();
        }
        initFile();
        try {
            long newPage = firstFree;
            file.seek(getPagePosition(newPage));
            long nextPage = file.readLong();
            file.seek(getPagePosition(newPage));
            file.writeLong(NIL);
            file.seek(posMap.get("firstFree"));
            file.writeLong(nextPage);
            firstFree = nextPage;
            file.seek(posMap.get("usedPages"));
            file.writeLong(++usedPages);
            return newPage;
        } catch (IOException ex) {
            ex.printStackTrace();
            return -1L;
        }
    }

    public void recyclePage(long pageIndex) {
        initFile();
        try {
            long nextPage = firstFree;
            file.seek(getPagePosition(pageIndex));
            file.writeLong(nextPage);
            file.seek(posMap.get("firstFree"));
            file.writeLong(pageIndex);
            firstFree = pageIndex;
            file.seek(posMap.get("usedPages"));
            file.writeLong(--usedPages);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public BTNode readPage(long page) {
        initFile();
        try {
            file.seek(getPagePosition(page));
            BTNode bt = new BTNode(m);
            bt.nextFree = file.readLong();
            bt.pageIndex = file.readLong();
            bt.size = file.readInt();
            bt.next = file.readLong();
            bt.prev = file.readLong();
            bt.isleaf = (file.readInt() != 0);
            file.readLong(); // 8B冗余
            for (int i = 0; i < bt.size; ++i) {
                bt.keys[i] = file.readLong();
                bt.ptrs[i] = file.readLong();
            }
            return bt;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void writePage(BTNode bt) {
        initFile();
        try {
            file.seek(getPagePosition(bt.pageIndex));
            file.writeLong(bt.nextFree);
            file.writeLong(bt.pageIndex);
            file.writeInt(bt.size);
            file.writeLong(bt.next);
            file.writeLong(bt.prev);
            file.writeInt(bt.isleaf ? 1 : 0);
            file.writeLong(0L); // 8B冗余
            for (int i = 0; i < bt.size; ++i) {
                file.writeLong(bt.keys[i]);
                file.writeLong(bt.ptrs[i]);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    static class FileMapperFactory {
        private int pageSize = _1KB; // 1KB
        private int keySize = 8; // 8B
        private int ptrSize = 8; // 8B
        private long capacity = 1000L;
        private long usedPages = 1L;
        private long root = 0L;
        private long sqt = 0L;
        private long firstFree = 1L;
        private String fileName = "tree.bin";

        public FileMapper load() {
            FileMapper mapper = new FileMapper();
            readFile();
            set(mapper);
            return mapper;
        }

        public FileMapper create() {
            FileMapper mapper = new FileMapper();
            set(mapper);
            createFile();
            return mapper;
        }

        private void set(FileMapper mapper) {
            mapper.pageSize = this.pageSize;
            mapper.keySize = this.keySize;
            mapper.ptrSize = this.ptrSize;
            mapper.capacity = this.capacity;
            mapper.usedPages = this.usedPages;
            mapper.root = this.root;
            mapper.sqt = this.sqt;
            mapper.firstFree = this.firstFree;
            mapper.m = ((pageSize - BTNode.pageHeaderCapacity) >> 4);
            mapper.fileName = this.fileName;
        }

        // 读取B树文件头
        private void readFile() {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(fileName, "r");
                pageSize = file.readInt();
                keySize = file.readInt();
                ptrSize = file.readInt();
                capacity = file.readLong();
                usedPages = file.readLong();
                root = file.readLong();
                sqt = file.readLong();
                firstFree = file.readLong();
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (file != null)
                        file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // 创建B树文件, 并预先分配空间
        private void createFile() {
            RandomAccessFile file = null;
            try {
                file = new RandomAccessFile(fileName, "rw");
                file.writeInt(pageSize);
                file.writeInt(keySize);
                file.writeInt(ptrSize);
                file.writeLong(capacity);
                file.writeLong(usedPages);
                file.writeLong(root);
                file.writeLong(sqt);
                file.writeLong(firstFree);
                file.writeLong(0L);
                file.writeInt(0); // 12B冗余
                byte[] page = new byte[pageSize - 8];
                for (long i = 0; i < capacity; ++i) {
                    file.writeLong(i + 1);
                    file.write(page);
                }
                // 创建根
                file.seek(fileHeaderCapacity);
                BTNode bt = new BTNode(BTNode.getM(pageSize));
                file.writeLong(bt.nextFree);
                file.writeLong(bt.pageIndex);
                file.writeInt(bt.size);
                file.writeLong(bt.next);
                file.writeLong(bt.prev);
                file.writeInt(bt.isleaf ? 1 : 0);
                file.writeLong(0L); // 8B冗余
                for (int i = 0; i < bt.size; ++i) {
                    file.writeLong(bt.keys[i]);
                    file.writeLong(bt.ptrs[i]);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    if (file != null)
                        file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public FileMapperFactory setPageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public FileMapperFactory setKeySize(int keySize) {
            this.keySize = keySize;
            return this;
        }

        public FileMapperFactory setPtrSize(int ptrSize) {
            this.ptrSize = ptrSize;
            return this;
        }

        public FileMapperFactory setCapacity(long capacity) {
            this.capacity = capacity;
            return this;
        }

        public FileMapperFactory setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
    }

    public static void main(String[] args) {
        FileMapper mapper = new FileMapperFactory().load();
        BTNode btNode = mapper.readPage(mapper.root);
        btNode.isleaf = false;
        mapper.writePage(btNode);
        BTNode btNode2 = mapper.readPage(mapper.root);
        System.out.println(btNode2.pageIndex);
        System.out.println(btNode2.isleaf);
    }
}
