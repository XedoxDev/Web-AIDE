package org.xedox.apkbuilder.task;

import io.github.muntashirakon.zipalign.ZipAlign;
import org.xedox.apkbuilder.ApkBuilder;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.xedox.apkbuilder.util.BuildException;
import org.xedox.apkbuilder.util.TaskManager;

public class AlignTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public AlignTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        if (!builder.config.apkAlignEnable) {
            builder.taskManager.debug("Apk align no usign!");
            return;
        }
        File baseApk = new File(builder.config.buildPath, "base.apk");
        File alignedApk = new File(builder.config.buildPath, "base_aligned.apk");

        if (!baseApk.exists()) {
            throw new BuildException("Input APK does not exist: " + baseApk.getAbsolutePath());
        }

        if (alignedApk.exists()) {
            Files.delete(alignedApk.toPath());
        }
        boolean successful =
                ZipAlign.doZipAlign(baseApk.getAbsolutePath(), alignedApk.getAbsolutePath(), 4, true);
        if (!successful) {
            throw new BuildException("Failed to align apk");
        }
        if (!alignedApk.exists()) {
            throw new BuildException(
                    "Failed to create aligned APK: " + alignedApk.getAbsolutePath());
        }
        builder.taskManager.task(
                "base.apk verification...",
                () -> {
                    ZipAlign.isZipAligned(alignedApk.getAbsolutePath(), 4);
                    if (!successful) {
                        throw new BuildException("Failed to align apk");
                    }
                });

        Files.move(alignedApk.toPath(), baseApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
