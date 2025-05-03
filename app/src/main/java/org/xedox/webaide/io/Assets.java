package org.xedox.webaide.io;

import android.content.Context;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Assets {

    private final Context context;
    private String assetName;
    private String toFileName;
    private String toPath;

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

    public byte[] readBytes() throws IOException {
        try (InputStream is = context.getAssets().open(assetName);
                BufferedInputStream bis = new BufferedInputStream(is);
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
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
        return new String(readBytes());
    }

    public void copy() throws IOException {
        File outputFile = new File(toPath, toFileName);
        copyBinary(outputFile);
    }

    public void copyBinary(File outputFile) throws IOException {
        File parentDir = outputFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            fos.write(readBytes());
        } catch (IOException e) {
            throw new IOException(
                    "Error copy binary asset: " + assetName + " to " + outputFile.getAbsolutePath(),
                    e);
        }
    }
}
