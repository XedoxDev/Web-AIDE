package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import java.io.File;
import org.xedox.apkbuilder.util.BuildException;
import java.io.PrintWriter;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.javac.JavaCompiler;
import org.xedox.javac.JavacOptionsBuilder;

public class CompileSourcesTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public CompileSourcesTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        File rJavaFile = new File(builder.genDir, builder.config.appPackage.replace('.', '/') + "/R.java");
        if (!rJavaFile.exists()) {
            throw new BuildException("R.java not found at: " + rJavaFile.getAbsolutePath());
        }

        PrintWriter outWriter = new PrintWriter(builder.taskManager.getPrintStream());
        PrintWriter errWriter = new PrintWriter(builder.taskManager.getPrintStream());
        JavaCompiler javac = new JavaCompiler(outWriter, errWriter);

        JavacOptionsBuilder mainOptions =
                JavacOptionsBuilder.create()
                        .destination(builder.classesDir.getAbsolutePath())
                        .classpath(builder.config.androidJarPath)
                        .option("-proc:none", "-warn:-unused")
                        .target(builder.config.javaVersion)
                        .source(builder.config.javaVersion);

        for (String sourcePath : builder.config.javaSources) {
            mainOptions.addSrc(sourcePath);
        }
        mainOptions.addSrc(rJavaFile.getAbsolutePath());

        if (builder.debugCommands) {
            builder.taskManager.debug("Using options: " + mainOptions.buildCmd());
        }

        if (!javac.compile(mainOptions.build())) {
            throw new BuildException("Main sources compilation failed");
        }
    }
}
