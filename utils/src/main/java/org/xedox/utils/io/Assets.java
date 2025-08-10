package org.xedox.utils;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipException;
import net.lingala.zip4j.ZipFile;

public final class Assets implements AutoCloseable, Closeable {
    private final Context context;
    private final AssetManager assetManager;

    public Assets(Context context) {
        this.context = context.getApplicationContext();
        this.assetManager = this.context.getAssets();
    }

    public static Assets from(Context context) {
        return new Assets(context);
    }

    public void copyAssetsRecursive(String assetPath, File targetDir) throws IOException {
        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Cannot create directory: " + targetDir);
        }

        String[] assets = assetManager.list(assetPath);
        if (assets == null || assets.length == 0) {
            return;
        }

        for (String asset : assets) {
            String childAssetPath = assetPath.isEmpty() ? asset : assetPath + "/" + asset;
            File childTargetFile = new File(targetDir, asset);

            InputStream is = null;
            try {
                is = assetManager.open(childAssetPath);
                copyStreamToFile(is, childTargetFile);
            } catch (IOException e) {
                copyAssetsRecursive(childAssetPath, childTargetFile);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    public boolean exists(String assetPath) {
        try {
            InputStream is = assetManager.open(assetPath);
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] readBytes(String assetPath) throws IOException {
        try (InputStream is = assetManager.open(assetPath);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
    }

    public String readText(String assetPath, Charset charset) throws IOException {
        return new String(readBytes(assetPath), charset);
    }

    public String readText(String assetPath) throws IOException {
        return readText(assetPath, StandardCharsets.UTF_8);
    }

    public void copyFile(String assetPath, File destination) throws IOException {
        copyFile(assetPath, destination, true);
    }

    public void copyFile(String assetPath, File destination, boolean overwrite) throws IOException {
        if (!overwrite && destination.exists()) {
            return;
        }

        File parent = destination.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Failed to create directory: " + parent);
            }
        }

        try (InputStream is = assetManager.open(assetPath);
                OutputStream os = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    public List<String> listFiles(String assetDir) throws IOException {
        try {
            String[] files = assetManager.list(assetDir);
            return files != null ? Arrays.asList(files) : Collections.emptyList();
        } catch (IOException e) {
            throw new IOException("Error listing assets in directory: " + assetDir, e);
        }
    }

    public List<String> listFilesRecursive(String assetDir) throws IOException {
        List<String> fileList = new ArrayList<>();
        listFilesRecursive(assetDir, fileList);
        return fileList;
    }

    private void listFilesRecursive(String assetDir, List<String> fileList) throws IOException {
        for (String asset : listFiles(assetDir)) {
            String childAssetPath = assetDir.isEmpty() ? asset : assetDir + "/" + asset;
            if (exists(childAssetPath)) {
                fileList.add(childAssetPath);
            } else {
                listFilesRecursive(childAssetPath, fileList);
            }
        }
    }

    public void copyDirectory(String assetDir, File destinationDir) throws IOException {
        copyDirectory(assetDir, destinationDir, true);
    }

    public void copyDirectory(String assetDir, File destinationDir, boolean overwrite)
            throws IOException {
        if (!destinationDir.exists() && !destinationDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + destinationDir);
        }

        for (String asset : listFiles(assetDir)) {
            String childAssetPath = assetDir.isEmpty() ? asset : assetDir + "/" + asset;
            File childDest = new File(destinationDir, asset);

            if (exists(childAssetPath)) {
                copyFile(childAssetPath, childDest, overwrite);
            } else {
                copyDirectory(childAssetPath, childDest, overwrite);
            }
        }
    }

    public List<String> readLines(String assetPath) throws IOException {
        return readLines(assetPath, StandardCharsets.UTF_8);
    }

    public List<String> readLines(String assetPath, Charset charset) throws IOException {
        List<String> lines = new ArrayList<>();
        try (InputStream is = assetManager.open(assetPath);
                Reader reader = new InputStreamReader(is, charset);
                BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static void copyStreamToFile(InputStream is, File targetFile) throws IOException {
        try (OutputStream os = new FileOutputStream(targetFile)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    public static void unzipFromAssets(Context context, String assetZipName, String destinationPath)
            throws IOException, ZipException {
        File tempZipFile = new File(context.getCacheDir(), "temp.res.zip");
        tempZipFile.createNewFile();

        try (InputStream is = context.getAssets().open(assetZipName)) {
            copyStreamToFile(is, tempZipFile);
        }

        if (!tempZipFile.exists()) {
            throw new IOException("Failed to create temp ZIP file.");
        }

        try {
            ZipFile zipFile = new ZipFile(tempZipFile);
            zipFile.extractAll(destinationPath);
        } finally {
            tempZipFile.delete();
        }
    }

    @Override
    public void close() throws IOException {
        assetManager.close();
    }
}
