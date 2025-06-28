package org.xedox.apkbuilder;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.xedox.apkbuilder.task.AlignTask;
import org.xedox.apkbuilder.task.CleanTask;
import org.xedox.apkbuilder.task.CompileResourcesTask;
import org.xedox.apkbuilder.task.LinkResourcesTask;
import org.xedox.apkbuilder.task.CompileSourcesTask;
import org.xedox.apkbuilder.task.DexingClassesTask;
import org.xedox.apkbuilder.task.PackageResourcesTask;
import org.xedox.apkbuilder.task.SignTask;
import org.xedox.apkbuilder.util.BinaryUtils;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.apkbuilder.util.BuildException;
import org.xedox.javac.JavacOptionsBuilder;

public class ApkBuilder {
    public final TaskManager taskManager;
    public final BuildConfig config;
    public boolean debugCommands;
    public File aapt2Binary;
    public File zipAlignBinary;
    public File compiledResDir;
    public File genDir;
    public File outputApk;
    public File classesDir;
    public File dexDir;
    public Context context;
    public static final Object keyCopyLock = new Object();

    public ApkBuilder(Context context, BuildConfig config) {
        this(context, System.out, config);
    }

    public ApkBuilder(Context context, PrintStream printStream, BuildConfig config) {
        this.context = context;
        this.taskManager = new TaskManager(Objects.requireNonNull(printStream));
        this.config = config != null ? config : new BuildConfig();
        String nativeDir = context.getApplicationInfo().nativeLibraryDir;
        aapt2Binary = new File(nativeDir, "libaapt2.so");
        zipAlignBinary = new File(nativeDir, "libzipalign.so");
        debugCommands = config.debugMode;
        taskManager.setVerbose(config.debugMode);

        this.compiledResDir = new File(config.buildPath, "compiled_res");
        this.genDir = new File(config.buildPath, "gen");
        this.outputApk = new File(genDir, "resources.ap_");
        this.classesDir = new File(config.buildPath, "classes");
        this.dexDir = new File(config.buildPath, "dex");
    }

    public void build() {
        try {
            validateBuildEnvironment();
            taskManager.task(
                    "Building APK",
                    () -> {
                        taskManager.start();
                        taskManager.task("Cleaning build directory...", new CleanTask(this));
                        taskManager.task("Compiling resources...", new CompileResourcesTask(this));
                        taskManager.task("Linking resources...", new LinkResourcesTask(this));
                        taskManager.task("Compiling sources...", new CompileSourcesTask(this));
                        taskManager.task("Dexing classes...", new DexingClassesTask(this));
                        taskManager.task("Packing resources...", new PackageResourcesTask(this));
                        taskManager.task("Aligning resources...", new AlignTask(this));
                        taskManager.task("Apk signing...", new SignTask(this));
                    });
        } catch (Exception err) {
            err.printStackTrace(taskManager.getPrintStream());
        }

        taskManager.printStatistics();
    }

    private void validateBuildEnvironment() throws Exception {
        validateDirectory(config.buildPath, "Build directory");
        validateDirectory(config.resDir, "Resources directory");
        if (config.assetsDir != null) validateDirectory(config.assetsDir, "Assets directory");
        if (config.nativeLibsDir != null)
            validateDirectory(config.nativeLibsDir, "Native libs directory");
        validateFile(new File(config.androidJarPath), "android.jar");
        validateFile(new File(config.manifestPath), "AndroidManifest.xml");

        if (config.javaSources.isEmpty()) {
            throw new BuildException("No Java sources specified");
        }
        for (String sourcePath : config.javaSources) {
            validateDirectory(sourcePath, "Java source directory");
        }

        createDirectory(classesDir, "Classes directory");
        BinaryUtils.setExecutable(aapt2Binary);
        BinaryUtils.setExecutable(zipAlignBinary);
    }

    private void validateDirectory(String path, String description) throws Exception {
        if (path == null) throw new BuildException(description + " path is null");
        File dir = new File(path);
        if (!dir.exists()) throw new BuildException(description + " does not exist: " + path);
        if (!dir.isDirectory())
            throw new BuildException(description + " is not a directory: " + path);
    }

    private void validateFile(File file, String description) throws Exception {
        if (!file.exists())
            throw new BuildException(description + " not found at: " + file.getAbsolutePath());
    }

    private void createDirectory(File dir, String description) throws Exception {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BuildException(
                    "Failed to create " + description + ": " + dir.getAbsolutePath());
        }
    }

    public static class BuildConfig {
        // Definitely //
        public String androidJarPath;
        public String buildPath;
        public String manifestPath;
        public String resDir;
        public String appPackage = "com.example.app";
        public String packageId = "0x7f";
        // Definitely //
        
        public String versionName = "1.0";
        public String versionCode = "1";
        public String minSdk = "21";
        public String targetSdk = "33";
        public String javaVersion = "17";

        public String assetsDir;
        public String nativeLibsDir;
        public String desugarJdkLibsPath;
        public String proguardRulesPath;

        public boolean debugMode = true;
        public boolean r8enabled = false;
        public boolean apkAlignEnable = true;
        public boolean apkSignEnable = true;

        public final JavacOptionsBuilder java = JavacOptionsBuilder.create();
        public final List<String> javaSources = new ArrayList<>();
        public final KeyConfig keyConfig = new KeyConfig();

        public static class KeyConfig {
            public boolean useKeystore = false;
            public Keystore keystore;
            public KeyWithCert keyWithCert;

            public KeyConfig() {
                this.keystore = new Keystore();
                this.keyWithCert = new KeyWithCert();
            }

            public static class Keystore {
                public String path;
                public String alias;
                public String storePassword;
                public String keyPassword;
            }

            public static class KeyWithCert {
                public String keyPath;
                public String certPath;
            }
        }
    }
}
