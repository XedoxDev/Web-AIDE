package org.xedox.webaide.io;

import java.io.File;
import java.io.IOException;

public interface IFile {

    public static String TAG = "IFile";

    public String read();

    public boolean write(String txt);

    public boolean write(byte[] txt);

    public boolean isFile();

    public boolean isDir();

    public IFile[] ifiles();

    public File[] files();

    public String getPath();

    public IFile getFilePath();

    public String getFullPath();

    public IFile getFullFile();

    public File toFile();

    public void remove();

    public void removeDir();

    public boolean mkfile();

    public boolean mkdir();

    public boolean mkdirs();

    public String getName();

    public String getExtension();

    public String getNameNoExtension();

    public IFile parent();

    public static void deleteDir(String path) throws IOException {
        File file = new File(path);
        if(file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();

        if (files != null) {
            for (File f : files) {
                if (f.isFile()) {
                    f.delete();
                } else {
                    deleteDir(f.getAbsolutePath()); 
                }
            }
            file.delete();
        }
    }

    public boolean exists();
}
