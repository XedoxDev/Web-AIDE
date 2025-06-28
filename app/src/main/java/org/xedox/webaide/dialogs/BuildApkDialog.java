package org.xedox.webaide.dialogs;

import org.xedox.webaide.activity.BaseActivity;
import org.xedox.webaide.util.GitManager;
import com.google.android.material.textfield.TextInputEditText;
import android.widget.TextView;
import android.view.View;
import org.xedox.webaide.activity.EditorActivity;
import org.xedox.webaide.console.ConsoleLayout;
import org.xedox.webaide.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import android.content.DialogInterface;
import org.xedox.apkbuilder.ApkBuilder;
import static org.xedox.apkbuilder.ApkBuilder.*;

public class BuildApkDialog {
    public interface BuildConfigListener {
        void onBuildConfigReady(BuildConfig config);
    }

    public static void show(EditorActivity context, BuildConfigListener listener) {
        DialogBuilder builder = new DialogBuilder(context);
        builder.setTitle(R.string.build_apk);
        builder.setView(R.layout.dialog_build_apk); 

        TextInputEditText androidJarPath = builder.findViewById(R.id.androidJarPath);
        TextInputEditText buildPath = builder.findViewById(R.id.buildPath);
        TextInputEditText manifestPath = builder.findViewById(R.id.manifestPath);
        TextInputEditText resDir = builder.findViewById(R.id.resDir);
        TextInputEditText assetsDir = builder.findViewById(R.id.assetsDir);
        TextInputEditText nativeLibsDir = builder.findViewById(R.id.nativeLibsDir);
        TextInputEditText desugarJdkLibsPath = builder.findViewById(R.id.desugarJdkLibsPath);
        TextInputEditText proguardRulesPath = builder.findViewById(R.id.proguardRulesPath);

        TextInputEditText appPackage = builder.findViewById(R.id.appPackage);
        TextInputEditText packageId = builder.findViewById(R.id.packageId);
        TextInputEditText versionName = builder.findViewById(R.id.versionName);
        TextInputEditText versionCode = builder.findViewById(R.id.versionCode);
        TextInputEditText minSdk = builder.findViewById(R.id.minSdk);
        TextInputEditText targetSdk = builder.findViewById(R.id.targetSdk);
        TextInputEditText javaVersion = builder.findViewById(R.id.javaVersion);

        SwitchMaterial debugMode = builder.findViewById(R.id.debugMode);
        SwitchMaterial r8enabled = builder.findViewById(R.id.r8enabled);
        SwitchMaterial apkAlignEnable = builder.findViewById(R.id.apkAlignEnable);
        SwitchMaterial apkSignEnable = builder.findViewById(R.id.apkSignEnable);

        SwitchMaterial useKeystore = builder.findViewById(R.id.useKeystore);
        TextInputEditText keystorePath = builder.findViewById(R.id.keystorePath);
        TextInputEditText keystoreAlias = builder.findViewById(R.id.keystoreAlias);
        TextInputEditText storePassword = builder.findViewById(R.id.storePassword);
        TextInputEditText keyPassword = builder.findViewById(R.id.keyPassword);
        TextInputEditText keyPath = builder.findViewById(R.id.keyPath);
        TextInputEditText certPath = builder.findViewById(R.id.certPath);

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton(
                R.string.ok,
                (dialog, which) -> {
                    BuildConfig config = new BuildConfig();

                    config.androidJarPath = getTextOrNull(androidJarPath);
                    config.buildPath = getTextOrNull(buildPath);
                    config.manifestPath = getTextOrNull(manifestPath);
                    config.resDir = getTextOrNull(resDir);
                    config.assetsDir = getTextOrNull(assetsDir);
                    config.nativeLibsDir = getTextOrNull(nativeLibsDir);
                    config.desugarJdkLibsPath = getTextOrNull(desugarJdkLibsPath);
                    config.proguardRulesPath = getTextOrNull(proguardRulesPath);

                    config.appPackage = getTextOrDefault(appPackage, "com.example.app");
                    config.packageId = getTextOrDefault(packageId, "0x7f");
                    config.versionName = getTextOrDefault(versionName, "1.0");
                    config.versionCode = getTextOrDefault(versionCode, "1");
                    config.minSdk = getTextOrDefault(minSdk, "21");
                    config.targetSdk = getTextOrDefault(targetSdk, "33");
                    config.javaVersion = getTextOrDefault(javaVersion, "17");

                    config.debugMode = debugMode.isChecked();
                    config.r8enabled = r8enabled.isChecked();
                    config.apkAlignEnable = apkAlignEnable.isChecked();
                    config.apkSignEnable = apkSignEnable.isChecked();

                    config.keyConfig.useKeystore = useKeystore.isChecked();
                    if (config.keyConfig.useKeystore) {
                        config.keyConfig.keystore.path = getTextOrNull(keystorePath);
                        config.keyConfig.keystore.alias = getTextOrNull(keystoreAlias);
                        config.keyConfig.keystore.storePassword = getTextOrNull(storePassword);
                        config.keyConfig.keystore.keyPassword = getTextOrNull(keyPassword);
                    } else {
                        config.keyConfig.keyWithCert.keyPath = getTextOrNull(keyPath);
                        config.keyConfig.keyWithCert.certPath = getTextOrNull(certPath);
                    }

                    if (listener != null) {
                        listener.onBuildConfigReady(config);
                    }
                    dialog.dismiss(); 
                });

        builder.show();
    }

    private static String getTextOrNull(TextInputEditText editText) {
        if (editText == null || editText.getText() == null) {
            return null;
        }
        String text = editText.getText().toString().trim();
        return text.isEmpty() ? null : text;
    }

    private static String getTextOrDefault(TextInputEditText editText, String defaultValue) {
        String text = getTextOrNull(editText);
        return text != null ? text : defaultValue;
    }
}
