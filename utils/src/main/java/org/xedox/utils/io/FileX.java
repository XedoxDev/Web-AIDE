package org.xedox.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileX extends File {

    public FileX(File file) {
        super(file.getAbsolutePath());
    }

    public FileX(String path) {
        super(path);
    }

    public FileX(String parent, String child) {
        super(parent, child);
    }

    public FileX(File parent, String child) {
        super(parent, child);
    }

    public FileX(FileX parent, String child) {
        super(parent.toFile(), child);
    }

    public static FileX of(String path) {
        return new FileX(path);
    }

    public static FileX of(File file) {
        return new FileX(file);
    }

    public static FileX of(FileX FileX) {
        return new FileX(FileX.toFile());
    }

    public String read() throws IOException {
        StringBuilder sb = new StringBuilder((int) length());
        try (BufferedReader reader = new BufferedReader(new FileReader(this))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        return sb.toString();
    }

    public byte[] readBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream((int) length());
        try (InputStream in = new FileInputStream(this)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        return out.toByteArray();
    }

    public boolean write(String text) throws IOException {
        try (FileWriter writer = new FileWriter(this)) {
            writer.write(text);
            return true;
        }
    }

    public boolean write(byte[] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(this)) {
            out.write(data);
            return true;
        }
    }

    public boolean append(String content) throws IOException {
        try (FileWriter writer = new FileWriter(this, true)) {
            writer.write(content);
            return true;
        }
    }

    public boolean append(byte[] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(this, true)) {
            out.write(data);
            return true;
        }
    }

    public boolean isDir() {
        return isDirectory();
    }

    public boolean isEmpty() {
        if (isFile()) {
            return length() == 0;
        } else if (isDirectory()) {
            String[] children = list();
            return children == null || children.length == 0;
        }
        return false;
    }

    @Override
    public FileX[] listFiles() {
        File[] files = super.listFiles();
        if (files == null) return new FileX[0];
        FileX[] result = new FileX[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = new FileX(files[i]);
        }
        return result;
    }

    public FileX[] FileXs() {
        return listFiles();
    }

    public File[] files() {
        return super.listFiles();
    }

    @Override
    public FileX[] listFiles(FilenameFilter filter) {
        File[] files = super.listFiles(filter);
        if (files == null) return new FileX[0];
        FileX[] result = new FileX[files.length];
        for (int i = 0; i < files.length; i++) {
            result[i] = new FileX(files[i]);
        }
        return result;
    }

    public String getExtension() {
        String name = getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot) : "";
    }

    public String getNameWithoutExtension() {
        String name = getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }

    public String getNameNoExtension() {
        return getNameWithoutExtension();
    }

    public FileX getParentFile() {
        String parent = getParent();
        return parent != null ? new FileX(parent) : null;
    }

    public FileX parent() {
        return getParentFile();
    }

    public boolean mkfile() throws IOException {
        if (exists()) {
            return false;
        }
        getParentFile().mkdirs();
        return createNewFile();
    }

    public void remove() {
        delete();
    }

    public boolean deleteRecursively() {
        if (isDirectory()) {
            for (FileX child : listFiles()) {
                if (!child.deleteRecursively()) {
                    return false;
                }
            }
        }
        return delete();
    }

    public void removeDir() {
        deleteRecursively();
    }

    public boolean renameTo(String newName) {
        File dest = new File(getParent(), newName);
        return super.renameTo(dest);
    }

    public boolean moveTo(FileX destination) throws IOException {
        return super.renameTo(destination.toFile());
    }

    public boolean copyTo(FileX destination) throws IOException {
        FileX.copy(this, destination);
        return true;
    }

    public InputStream openInputStream() throws FileNotFoundException {
        return new FileInputStream(this);
    }

    public OutputStream openOutputStream() throws FileNotFoundException {
        return new FileOutputStream(this);
    }

    public OutputStream openOutputStream(boolean append) throws FileNotFoundException {
        return new FileOutputStream(this, append);
    }

    public FileX getFilePath() {
        return getParentFile();
    }

    public FileX getFullFile() {
        return this;
    }

    public File toFile() {
        return this;
    }

    public static void copy(File src, File dest) throws IOException {
        try (InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
    }

    public static void move(File src, File dest) throws IOException {
        if (!src.renameTo(dest)) {
            copy(src, dest);
            if (!src.delete()) {
                throw new IOException("Failed to delete source after copy: " + src);
            }
        }
    }

    public static String getExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot) : "";
    }

    public static String calculateChecksum(File file, String algorithm) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hash = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Unknown algorithm: " + algorithm, e);
        }
    }

    public static String read(File file) throws IOException {
        return new FileX(file).read();
    }

    public static boolean write(File file, String content) throws IOException {
        return new FileX(file).write(content);
    }

    public static boolean deleteDirectory(File dir) {
        return new FileX(dir).deleteRecursively();
    }

    public static String getRelativePath(File base, File target) {
        return base.toPath().relativize(target.toPath()).toString();
    }

    public String getRelativePath(File target) {
        return getRelativePath(this, target);
    }
}
