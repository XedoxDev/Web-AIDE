package org.xedox.webaide.util.io;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Assets {
    private final Context context;
    private String assetName;
    private String toFileName;
    private String toPath;
    private boolean overwriteExisting = true;

    public static Assets from(Context context) {
        return new Assets(context);
    }

    private Assets(Context context) {
        this.context = context.getApplicationContext();
    }

    public Assets asset(String name) {
        this.assetName = name;
        return this;
    }

    public Assets toFileName(String fileName) {
        this.toFileName = fileName;
        return this;
    }

    public Assets toPath(String path) {
        this.toPath = path;
        return this;
    }

    public Assets overwrite(boolean overwrite) {
        this.overwriteExisting = overwrite;
        return this;
    }

    public boolean assetExists() {
        try {
            String[] files = context.getAssets().list("");
            if (files != null) {
                for (String file : files) {
                    if (file.equals(assetName)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean assetExists(String assetPath) {
        try {
            InputStream is = context.getAssets().open(assetPath);
            is.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public List<String> listAssets(String path) throws IOException {
        List<String> assetsList = new ArrayList<>();
        try {
            String[] files = context.getAssets().list(path);
            if (files != null) {
                for (String file : files) {
                    assetsList.add(file);
                }
            }
        } catch (IOException e) {
            throw new IOException("Error listing assets in path: " + path, e);
        }
        return assetsList;
    }

    public byte[] readBytes() throws IOException {
        if (assetName == null) {
            throw new IllegalStateException("Asset name not specified");
        }

        try (InputStream is = context.getAssets().open(assetName);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IOException("Error reading asset: " + assetName, e);
        }
    }

    public String read() throws IOException {
        return new String(readBytes(), "UTF-8");
    }

    public String read(String charsetName) throws IOException {
        return new String(readBytes(), charsetName);
    }

    public void copy() throws IOException {
        if (toPath == null || toFileName == null) {
            throw new IllegalStateException("Destination path or filename not specified");
        }

        File outputFile = new File(toPath, toFileName);
        copyTo(outputFile);
    }

    public void copyTo(File outputFile) throws IOException {
        if (!overwriteExisting && outputFile.exists()) {
            return;
        }

        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        try (InputStream is = context.getAssets().open(assetName);
                FileOutputStream fos = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException(
                    "Error copying asset: " + assetName + " to " + outputFile.getAbsolutePath(), e);
        }
    }

    public void copyRecursively(String assetDir, File destinationDir) throws IOException {
        List<String> assets = listAssets(assetDir);
        for (String asset : assets) {
            String assetPath = assetDir.isEmpty() ? asset : assetDir + File.separator + asset;
            File destFile = new File(destinationDir, asset);

            if (assetExists(assetPath)) {
                asset(assetPath).copyTo(destFile);
            } else {
                copyRecursively(assetPath, destFile);
            }
        }
    }

    private static boolean copyAssetsRecursively(
            AssetManager assetManager, String assetPath, String targetPath) throws IOException {
        String[] list = assetManager.list(assetPath);
        if (list == null || list.length == 0) {
            try (InputStream in = assetManager.open(assetPath)) {
                File outFile = new File(targetPath);
                if (outFile.getParentFile() != null) {
                    outFile.getParentFile().mkdirs();
                }
                java.nio.file.Files.copy(in, outFile.toPath());
                return true;
            }
        } else {
            File targetDir = new File(targetPath);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            boolean success = true;
            for (String item : list) {
                String childAssetPath = assetPath + File.separator + item;
                String childTargetPath = targetPath + File.separator + item;
                success &= copyAssetsRecursively(assetManager, childAssetPath, childTargetPath);
            }
            return success;
        }
    }

    public boolean copyAssetsRecursively(String assetPath, String targetPath) throws IOException {
        return copyAssetsRecursively(context.getAssets(), assetPath, targetPath);
    }
}
