package org.xedox.apkbuilder.task;

import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.javac.OptionsBuilder;
import com.android.tools.r8.D8;
import com.android.tools.r8.R8;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DexingClassesTask implements TaskManager.Task {
    private final ApkBuilder builder;
    private boolean fallbackToD8 = false;

    public DexingClassesTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        builder.dexDir.mkdirs();
        Path classesDirPath = builder.classesDir.toPath();
        Path androidJar = Paths.get(builder.config.androidJarPath);

        List<Path> classFiles =
                Files.walk(classesDirPath)
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".class"))
                        .collect(Collectors.toList());

        if (classFiles.isEmpty()) {
            throw new IOException("No .class files found for dexing");
        }

        boolean useR8 = builder.config.r8enabled && !fallbackToD8;
        builder.taskManager.debug("Using " + (useR8 ? "R8" : "D8") + " for dexing...");

        OptionsBuilder options =
                new OptionsBuilder()
                        .arg("--output", builder.dexDir.toString())
                        .arg("--min-api", "21")
                        .arg("--lib", androidJar.toString());

        if (useR8) {
            try {
                options.arg("--release")
                        .arg("--classpath", androidJar.toString())
                        .arg("--no-desugaring");

                if (builder.config.proguardRulesPath != null) {
                    options.arg("--pg-conf", builder.config.proguardRulesPath);
                }
                File tempJar = createJar(classFiles, classesDirPath);
                options.arg(tempJar.getAbsolutePath());

                executeTool(options, true);

                if (!hasDexFiles()) {
                    throw new IOException("R8 failed to produce dex files");
                }
            } catch (Exception e) {
                builder.taskManager.error("R8 failed, falling back to D8: " + e.getMessage());
                fallbackToD8 = true;
                run();
            }
        } else {
            if (builder.config.desugarJdkLibsPath != null) {
                options.arg("--classpath", builder.config.desugarJdkLibsPath);
            }

            classFiles.forEach(p -> options.arg(p.toString()));

            executeTool(options, false);
        }
    }

    private File createJar(List<Path> classFiles, Path baseDir) throws IOException {
        File tempJar = File.createTempFile("classes", ".jar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(tempJar))) {
            for (Path classFile : classFiles) {
                String relativePath =
                        baseDir.relativize(classFile)
                                .toString()
                                .replace('\\', '/')
                                .replace(".class", "")
                                .replace("/", ".");
                if (!relativePath.startsWith("java.")
                        && !relativePath.startsWith("javax.")
                        && !relativePath.startsWith("android.")) {
                    String entryName = baseDir.relativize(classFile).toString().replace('\\', '/');
                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.write(Files.readAllBytes(classFile));
                    zos.closeEntry();
                }
            }
        }
        return tempJar;
    }

    private void executeTool(OptionsBuilder options, boolean isR8) throws Exception {
        String[] args = options.build();
        builder.taskManager.debug("Using options: " + String.join(" ", args));

        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        PrintStream originalErr = System.err;

        try {
            
            if (isR8) {
                R8.main(args);
            } else {
                D8.main(args);
            }
        } finally {
            System.setErr(originalErr);

            if (!errors.toString().isEmpty()) {
                builder.taskManager.debug((isR8 ? "R8" : "D8") + ": " + errors);
            }
        }
    }

    private boolean hasDexFiles() {
        File[] dexFiles = builder.dexDir.listFiles((dir, name) -> name.endsWith(".dex"));
        return dexFiles != null && dexFiles.length > 0;
    }
}
