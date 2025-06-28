package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import net.lingala.zip4j.ZipFile;
import org.xedox.apkbuilder.util.BuildException;
import java.util.Arrays;
import java.util.Comparator;
import net.lingala.zip4j.model.ZipParameters;
import org.xedox.apkbuilder.util.TaskManager;

public class PackageResourcesTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public PackageResourcesTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        File baseApk = new File(builder.config.buildPath, "base.apk");
        Files.copy(builder.outputApk.toPath(), baseApk.toPath(), StandardCopyOption.REPLACE_EXISTING);

        try (ZipFile zip = new ZipFile(baseApk)) {
            File[] dexFiles = builder.dexDir.listFiles((dir, name) -> name.endsWith(".dex"));
            if (dexFiles == null || dexFiles.length == 0) {
                throw new BuildException("No dex files found in " + builder.dexDir);
            }

            Arrays.sort(dexFiles, Comparator.comparing(File::getName));

            for (int i = 0; i < dexFiles.length; i++) {
                String entryName = i == 0 ? "classes.dex" : "classes" + (i + 1) + ".dex";
                zip.addFile(
                        dexFiles[i],
                        new ZipParameters() {
                            {
                                setFileNameInZip(entryName);
                            }
                        });
            }

            if (builder.config.assetsDir != null) {
                zip.addFolder(new File(builder.config.assetsDir));
            }

            if (builder.config.nativeLibsDir != null) {
                for (File archDir : new File(builder.config.nativeLibsDir).listFiles(File::isDirectory)) {
                    zip.addFolder(archDir);
                }
            }
        }
    }
}