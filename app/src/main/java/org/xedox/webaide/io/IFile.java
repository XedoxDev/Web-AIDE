package org.xedox.webaide.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.FileOutputStream;

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
        if (file.isFile()) {
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

    public static void unzip(String path, String out) {
        try {
            unzip(new FileInputStream(path), out);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static void unzip(InputStream input, String out) {
        try (ZipInputStream zis = new ZipInputStream(input)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                File destFile = new File(out, entry.getName());

                if (entry.isDirectory()) {
                    destFile.mkdirs();
                } else {
                    destFile.getParentFile().mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(destFile)) {
                        int len;
                        while ((len = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyAsset(Context context, String path, String out) {
        try (InputStream in = context.getAssets().open(path);
                FileOutputStream fos = new FileOutputStream(out)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
