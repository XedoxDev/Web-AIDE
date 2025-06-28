package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.apkbuilder.util.BuildException;
import org.xedox.apkbuilder.util.TaskManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class CleanTask implements TaskManager.Task {

    private final ApkBuilder builder;

    public CleanTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        File buildDir = new File(builder.config.buildPath);

        if (!buildDir.exists()) {
            return;
        }

        deleteDirectory(buildDir);
        Files.createDirectories(buildDir.toPath());
    }

    private void deleteDirectory(File directory) throws BuildException {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        throw new BuildException(
                                "Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
        if (!directory.delete()) {
            throw new BuildException("Failed to delete directory: " + directory.getAbsolutePath());
        }
    }
}
