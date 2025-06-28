package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import java.io.File;
import org.xedox.apkbuilder.util.BuildException;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.javac.OptionsBuilder;
import org.xedox.apkbuilder.util.BinaryUtils;

public class LinkResourcesTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public LinkResourcesTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        File[] flatFiles = builder.compiledResDir.listFiles((dir, name) -> name.endsWith(".flat"));
        builder.genDir.mkdirs();
        OptionsBuilder opt = new OptionsBuilder();
        opt.arg(builder.aapt2Binary.getAbsolutePath());
        opt.arg("link");
        for (File flatFile : flatFiles) {
            opt.arg(flatFile.getAbsolutePath());
        }
        opt.arg("-o", builder.outputApk.getAbsolutePath());
        opt.arg("--java", builder.genDir.getAbsolutePath());
        opt.arg("-I", builder.config.androidJarPath);
        opt.arg("--manifest", builder.config.manifestPath);
        opt.arg("--package-id", builder.config.packageId);
        opt.arg("--version-code", builder.config.versionCode);
        opt.arg("--version-name", builder.config.versionName);
        opt.arg("--min-sdk-version", builder.config.minSdk);
        opt.arg("--target-sdk-version", builder.config.targetSdk);
        opt.arg("--rename-manifest-package", builder.config.appPackage);

        if (builder.debugCommands) {
            builder.taskManager.debug("Using options: " + opt.buildCmd());
        }

        String aapt2Output = BinaryUtils.execute(opt.build());
        String output = BinaryUtils.execute(opt.build());
        if (output != null && output.length() != 0) {
            builder.taskManager.debug("aapt2 link output: " + output);
        }
        if (!builder.outputApk.exists()) {
            throw new BuildException("Output APK not created");
        }
    }
}
