package org.xedox.filetree.utils;

import java.io.File;

public class Node extends File {

    public Node(File file) {
        super(file.getAbsolutePath());
    }

    public Node(String path) {
        super(path);
    }

    public Node(String parent, String child) {
        super(parent, child);
    }

    public Node(File parent, String child) {
        super(parent, child);
    }

    private boolean isOpen = false;
    private int level = 0;

    public boolean isOpen() {
        return this.isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
