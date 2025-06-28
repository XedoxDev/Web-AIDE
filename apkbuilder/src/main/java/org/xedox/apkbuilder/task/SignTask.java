package org.xedox.apkbuilder.task;

import com.android.apksigner.ApkSignerTool;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import org.xedox.apkbuilder.ApkBuilder;
import org.xedox.apkbuilder.util.TaskManager;
import org.xedox.apkbuilder.util.BuildException;
import java.io.File;
import org.xedox.javac.OptionsBuilder;

public class SignTask implements TaskManager.Task {
    private final ApkBuilder builder;

    public SignTask(ApkBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void run() throws Exception {
        if (!builder.config.apkSignEnable) {
            builder.taskManager.debug("APK signing disabled, skipping...");
            return;
        }

        File unsigned = new File(builder.config.buildPath, "base.apk");
        File signedApk = new File(builder.config.buildPath, "signed_" + unsigned.getName());

        if (builder.config.keyConfig.useKeystore) {
            signWithKeystore(unsigned, signedApk);
        } else {
            signWithKeyCert(unsigned, signedApk);
        }
    }

    private void signWithKeystore(File unsignedApk, File signedApk) throws BuildException {
        ApkBuilder.BuildConfig.KeyConfig.Keystore ks = builder.config.keyConfig.keystore;
        
        if (ks.path == null) {
            File debugKeystore = new File(builder.config.buildPath, "debug.keystore");
            if (!debugKeystore.exists()) {
                createDebugKeystore(debugKeystore);
            }
            ks.path = debugKeystore.getAbsolutePath();
            ks.alias = "androiddebugkey";
            ks.storePassword = "android";
            ks.keyPassword = "android";
        }

        OptionsBuilder options = new OptionsBuilder()
            .arg("sign")
            .arg("--out", signedApk.getAbsolutePath())
            .arg("--in", unsignedApk.getAbsolutePath())
            .arg("--ks", ks.path)
            .arg("--ks-key-alias", ks.alias)
            .arg("--ks-pass", "pass:" + ks.storePassword)
            .arg("--key-pass", "pass:" + ks.keyPassword);

        executeSigning(options);
    }

    private void signWithKeyCert(File unsignedApk, File signedApk) throws BuildException {
        ApkBuilder.BuildConfig.KeyConfig.KeyWithCert kc = builder.config.keyConfig.keyWithCert;
        
        File keyFile = new File(builder.config.buildPath, "testkey.pk8");
        File certFile = new File(builder.config.buildPath, "testkey.x509.pem");
        
        if (!keyFile.exists() || !certFile.exists()) {
            createDefaultKeyAndCert(keyFile, certFile);
        }
        
        if (!keyFile.exists() || !certFile.exists()) {
            throw new BuildException("Key or certificate file not found");
        }

        OptionsBuilder options = new OptionsBuilder()
            .arg("sign")
            .arg("--out", signedApk.getAbsolutePath())
            .arg("--in", unsignedApk.getAbsolutePath())
            .arg("--key", keyFile.getAbsolutePath())
            .arg("--cert", certFile.getAbsolutePath())
            .arg("--v1-signing-enabled", "true")
            .arg("--v2-signing-enabled", "true");

        executeSigning(options);
    }

    private void createDebugKeystore(File keystoreFile) throws BuildException {
        try (InputStream is = builder.context.getAssets().open("debug.keystore");
             OutputStream out = new FileOutputStream(keystoreFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new BuildException("Failed to create debug keystore", e);
        }
    }

    private void createDefaultKeyAndCert(File keyFile, File certFile) throws BuildException {
        try {
            if (!keyFile.exists()) {
                try (InputStream is = builder.context.getAssets().open("testkey.pk8");
                     OutputStream out = new FileOutputStream(keyFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }

            if (!certFile.exists()) {
                try (InputStream is = builder.context.getAssets().open("testkey.x509.pem");
                     OutputStream out = new FileOutputStream(certFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
            }
        } catch (Exception e) {
            throw new BuildException("Failed to create default key/cert pair", e);
        }
    }

    private void executeSigning(OptionsBuilder options) throws BuildException {
        builder.taskManager.debug("Signing APK with command: " + options.buildCmd());
        
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errors));
        
        try {
            ApkSignerTool.main(options.build());
        } catch (Throwable err) {
            throw new BuildException("Failed to sign APK: " + err.getMessage(), err);
        } finally {
            System.setErr(originalErr);
        }

        if (!errors.toString().isEmpty()) {
            builder.taskManager.debug("ApkSigner output: " + errors);
        }
    }
}