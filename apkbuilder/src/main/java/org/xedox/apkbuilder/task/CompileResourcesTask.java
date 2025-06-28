package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.javac.OptionsBuilder;
import org.xedox.apkbuilder.util.BinaryUtils;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.apkbuilder.util.BuildException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Collectors;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import com.android.tools.r8.D8;

public class CompileResourcesTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public CompileResourcesTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        builder.compiledResDir.mkdirs();
        OptionsBuilder opt = new OptionsBuilder();
        opt.arg(builder.aapt2Binary.getAbsolutePath());
        opt.arg("compile");
        opt.arg("--dir", builder.config.resDir);
        opt.arg("-o", builder.compiledResDir.getAbsolutePath());

        if (builder.debugCommands) {
            builder.taskManager.debug("Using options: " + opt.buildCmd());
        }

        String output = BinaryUtils.execute(opt.build());
        if(output!= null && output.length() != 0) {
            builder.taskManager.debug("aapt2 compile output: " + output);
        }
        
    }
}