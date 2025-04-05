package org.xedox.webaide.io;

import android.net.Uri;
import android.content.Context;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

public class FileX extends File implements IFile {

    public FileX(File file) {
        super(file.getAbsolutePath());
    }

    public FileX(String file) {
        super(file);
    }

    public FileX(String path, String file) {
        super(path, file);
    }

    public FileX(File path, String name) {
        super(path, name);
    }

    public FileX(IFile ifile, String name) {
        super(ifile.toFile(), name);
    }

    public FileX(IFile ifile) {
        super(ifile.getFullPath());
    }

    @Override
    public String read() {
        StringBuilder buffer = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(this))) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line).append("\n");
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        return buffer.toString().trim();
    }

    @Override
    public boolean write(String txt) {
        try (FileWriter fw = new FileWriter(this)) {
            fw.write(txt);
            return true;
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isDir() {
        return isDirectory();
    }

    @Override
    public IFile[] ifiles() {
        File[] files = listFiles();
        IFile[] ifiles = new IFile[files.length];
        for (int i = 0; i < files.length; i++) {
            ifiles[i] = new FileX(files[i]);
        }
        return ifiles;
    }

    @Override
    public File[] files() {
        return listFiles();
    }

    @Override
    public IFile getFilePath() {
        return new FileX(getParent());
    }

    @Override
    public String getFullPath() {
        return getAbsolutePath();
    }

    @Override
    public IFile getFullFile() {
        return new FileX(getAbsolutePath());
    }

    @Override
    public File toFile() {
        return new File(getAbsolutePath());
    }

    @Override
    public void remove() {
        if (isFile()) {
            delete();
        }
    }

    @Override
    public void removeDir() {
        if (isDir()) {
            try {
                IFile.deleteDir(getFullPath());
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            remove();
        }
    }

    @Override
    public boolean mkfile() {
        try {
            return createNewFile();
        } catch (Exception err) {
            err.printStackTrace();
            return false;
        }
    }

    @Override
    public String getExtension() {
        String n = getName();
        int dotIndex = n.lastIndexOf(".");
        if (dotIndex == -1) {
            return ".txt";
        }
        return n.substring(dotIndex, n.length());
    }

    @Override
    public String getNameNoExtension() {
        String n = getName();
        return n.substring(0, n.lastIndexOf(".") - 1);
    }

    @Override
    public IFile parent() {
        return new FileX(getParent());
    }

    public boolean write(byte[] txt) {
        try (OutputStream os = new FileOutputStream(this)) {
            os.write(txt);
            return true;
        } catch (IOException e) {
            System.err.println("File write exception: " + e.getMessage());
            return false;
        }
    }
}
