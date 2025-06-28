/*
 * Decompiled with CFR 0.152.
 */
package com.android.apksigner;

import com.android.apksig.ApkSigner;
import com.android.apksig.ApkVerifier;
import com.android.apksig.SigningCertificateLineage;
import com.android.apksig.apk.ApkFormatException;
import com.android.apksig.apk.MinSdkVersionException;
import com.android.apksig.util.DataSource;
import com.android.apksig.util.DataSources;
import com.android.apksigner.HexEncoding;
import com.android.apksigner.OptionsParser;
import com.android.apksigner.ParameterException;
import com.android.apksigner.PasswordRetriever;
import com.android.apksigner.SignerParams;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAKey;
import java.security.interfaces.DSAParams;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import org.conscrypt.OpenSSLProvider;

public class ApkSignerTool {
    private static final String VERSION = "0.9";
    private static final String HELP_PAGE_GENERAL = "help.txt";
    private static final String HELP_PAGE_SIGN = "help_sign.txt";
    private static final String HELP_PAGE_VERIFY = "help_verify.txt";
    private static final String HELP_PAGE_ROTATE = "help_rotate.txt";
    private static final String HELP_PAGE_LINEAGE = "help_lineage.txt";
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    private static MessageDigest sha256 = null;
    private static MessageDigest sha1 = null;
    private static MessageDigest md5 = null;
    public static final int ZIP_MAGIC = 67324752;

    public static void main(String[] params) throws Exception {
        if (params.length == 0 || "--help".equals(params[0]) || "-h".equals(params[0])) {
            ApkSignerTool.printUsage(HELP_PAGE_GENERAL);
            return;
        }
        ApkSignerTool.addProviders();
        String cmd = params[0];
        try {
            if ("sign".equals(cmd)) {
                ApkSignerTool.sign(Arrays.copyOfRange(params, 1, params.length));
                return;
            }
            if ("verify".equals(cmd)) {
                ApkSignerTool.verify(Arrays.copyOfRange(params, 1, params.length));
                return;
            }
            if ("version".equals(cmd)) {
                System.out.println(VERSION);
                return;
            }
            throw new ParameterException("Unsupported command: " + cmd + ". See --help for supported commands");
        }
        catch (OptionsParser.OptionsException | ParameterException e) {
            System.err.println(e.getMessage());
            //System.exit(1);
            return;
        }
    }

    private static void addProviders() {
        try {
            Security.addProvider(new OpenSSLProvider());
        }
        catch (UnsatisfiedLinkError unsatisfiedLinkError) {
            // empty catch block
        }
    }

    public static void sign(String[] params) throws Exception {
        File tmpOutputApk;
        String optionName;
        if (params.length == 0) {
            ApkSignerTool.printUsage(HELP_PAGE_SIGN);
            return;
        }
        File outputApk = null;
        File inputApk = null;
        boolean verbose = false;
        boolean v1SigningEnabled = true;
        boolean v2SigningEnabled = true;
        boolean v3SigningEnabled = true;
        boolean v4SigningEnabled = true;
        boolean forceSourceStampOverwrite = false;
        boolean sourceStampTimestampEnabled = true;
        boolean alignFileSize = false;
        boolean verityEnabled = false;
        boolean debuggableApkPermitted = true;
        boolean alignmentPreserved = false;
        int libPageAlignment = 16384;
        int minSdkVersion = 1;
        boolean minSdkVersionSpecified = false;
        int maxSdkVersion = Integer.MAX_VALUE;
        int rotationMinSdkVersion = 33;
        boolean rotationTargetsDevRelease = false;
        ArrayList<SignerParams> signers = new ArrayList<SignerParams>(1);
        SignerParams signerParams = new SignerParams();
        SigningCertificateLineage lineage = null;
        SignerParams sourceStampSignerParams = new SignerParams();
        SigningCertificateLineage sourceStampLineage = null;
        ArrayList<ProviderInstallSpec> providers = new ArrayList<ProviderInstallSpec>();
        ProviderInstallSpec providerParams = new ProviderInstallSpec();
        OptionsParser optionsParser = new OptionsParser(params);
        String optionOriginalForm = null;
        boolean v4SigningFlagFound = false;
        boolean sourceStampFlagFound = false;
        boolean deterministicDsaSigning = false;
        boolean otherSignersSignaturesPreserved = false;
        while ((optionName = optionsParser.nextOption()) != null) {
            File lineageFile;
            optionOriginalForm = optionsParser.getOptionOriginalForm();
            if ("help".equals(optionName) || "h".equals(optionName)) {
                ApkSignerTool.printUsage(HELP_PAGE_SIGN);
                return;
            }
            if ("out".equals(optionName)) {
                outputApk = new File(optionsParser.getRequiredValue("Output file name"));
                continue;
            }
            if ("in".equals(optionName)) {
                inputApk = new File(optionsParser.getRequiredValue("Input file name"));
                continue;
            }
            if ("min-sdk-version".equals(optionName)) {
                minSdkVersion = optionsParser.getRequiredIntValue("Mininimum API Level");
                minSdkVersionSpecified = true;
                continue;
            }
            if ("max-sdk-version".equals(optionName)) {
                maxSdkVersion = optionsParser.getRequiredIntValue("Maximum API Level");
                continue;
            }
            if ("rotation-min-sdk-version".equals(optionName)) {
                rotationMinSdkVersion = optionsParser.getRequiredIntValue("Minimum API Level for Rotation");
                continue;
            }
            if ("rotation-targets-dev-release".equals(optionName)) {
                rotationTargetsDevRelease = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("v1-signing-enabled".equals(optionName)) {
                v1SigningEnabled = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("v2-signing-enabled".equals(optionName)) {
                v2SigningEnabled = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("v3-signing-enabled".equals(optionName)) {
                v3SigningEnabled = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("v4-signing-enabled".equals(optionName)) {
                v4SigningEnabled = optionsParser.getOptionalBooleanValue(true);
                v4SigningFlagFound = true;
                continue;
            }
            if ("force-stamp-overwrite".equals(optionName)) {
                forceSourceStampOverwrite = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("stamp-timestamp-enabled".equals(optionName)) {
                sourceStampTimestampEnabled = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("align-file-size".equals(optionName)) {
                alignFileSize = true;
                continue;
            }
            if ("verity-enabled".equals(optionName)) {
                verityEnabled = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("debuggable-apk-permitted".equals(optionName)) {
                debuggableApkPermitted = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("alignment-preserved".equals(optionName)) {
                alignmentPreserved = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("lib-page-alignment".equals(optionName)) {
                libPageAlignment = optionsParser.getRequiredIntValue("Native library page alignment size in bytes");
                continue;
            }
            if ("next-signer".equals(optionName)) {
                if (signerParams.isEmpty()) continue;
                signers.add(signerParams);
                signerParams = new SignerParams();
                continue;
            }
            if ("signer-for-min-sdk-version".equals(optionName)) {
                if (!signerParams.isEmpty()) {
                    signers.add(signerParams);
                    signerParams = new SignerParams();
                }
                signerParams.setMinSdkVersion(optionsParser.getRequiredIntValue("Mininimum API Level for signing config"));
                continue;
            }
            if ("ks".equals(optionName)) {
                signerParams.setKeystoreFile(optionsParser.getRequiredValue("KeyStore file"));
                continue;
            }
            if ("ks-key-alias".equals(optionName)) {
                signerParams.setKeystoreKeyAlias(optionsParser.getRequiredValue("KeyStore key alias"));
                continue;
            }
            if ("ks-pass".equals(optionName)) {
                signerParams.setKeystorePasswordSpec(optionsParser.getRequiredValue("KeyStore password"));
                continue;
            }
            if ("key-pass".equals(optionName)) {
                signerParams.setKeyPasswordSpec(optionsParser.getRequiredValue("Key password"));
                continue;
            }
            if ("pass-encoding".equals(optionName)) {
                String charsetName = optionsParser.getRequiredValue("Password character encoding");
                try {
                    signerParams.setPasswordCharset(PasswordRetriever.getCharsetByName(charsetName));
                    continue;
                }
                catch (IllegalArgumentException e) {
                    throw new ParameterException("Unsupported password character encoding requested using --pass-encoding: " + charsetName);
                }
            }
            if ("v1-signer-name".equals(optionName)) {
                signerParams.setV1SigFileBasename(optionsParser.getRequiredValue("JAR signature file basename"));
                continue;
            }
            if ("ks-type".equals(optionName)) {
                signerParams.setKeystoreType(optionsParser.getRequiredValue("KeyStore type"));
                continue;
            }
            if ("ks-provider-name".equals(optionName)) {
                signerParams.setKeystoreProviderName(optionsParser.getRequiredValue("JCA KeyStore Provider name"));
                continue;
            }
            if ("ks-provider-class".equals(optionName)) {
                signerParams.setKeystoreProviderClass(optionsParser.getRequiredValue("JCA KeyStore Provider class name"));
                continue;
            }
            if ("ks-provider-arg".equals(optionName)) {
                signerParams.setKeystoreProviderArg(optionsParser.getRequiredValue("JCA KeyStore Provider constructor argument"));
                continue;
            }
            if ("kms-type".equals(optionName)) {
                signerParams.setKmsType(optionsParser.getRequiredValue("Key Management Service (KMS) type"));
                continue;
            }
            if ("kms-key-alias".equals(optionName)) {
                signerParams.setKmsKeyAlias(optionsParser.getRequiredValue("Key Management Service (KMS) key alias"));
                continue;
            }
            if ("key".equals(optionName)) {
                signerParams.setKeyFile(optionsParser.getRequiredValue("Private key file"));
                continue;
            }
            if ("cert".equals(optionName)) {
                signerParams.setCertFile(optionsParser.getRequiredValue("Certificate file"));
                continue;
            }
            if ("signer-lineage".equals(optionName)) {
                lineageFile = new File(optionsParser.getRequiredValue("Lineage file for signing config"));
                signerParams.setSigningCertificateLineage(ApkSignerTool.getLineageFromInputFile(lineageFile));
                continue;
            }
            if ("lineage".equals(optionName)) {
                lineageFile = new File(optionsParser.getRequiredValue("Lineage file"));
                lineage = ApkSignerTool.getLineageFromInputFile(lineageFile);
                continue;
            }
            if ("v".equals(optionName) || "verbose".equals(optionName)) {
                verbose = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("next-provider".equals(optionName)) {
                if (providerParams.isEmpty()) continue;
                providers.add(providerParams);
                providerParams = new ProviderInstallSpec();
                continue;
            }
            if ("provider-class".equals(optionName)) {
                providerParams.className = optionsParser.getRequiredValue("JCA Provider class name");
                continue;
            }
            if ("provider-arg".equals(optionName)) {
                providerParams.constructorParam = optionsParser.getRequiredValue("JCA Provider constructor argument");
                continue;
            }
            if ("provider-pos".equals(optionName)) {
                providerParams.position = optionsParser.getRequiredIntValue("JCA Provider position");
                continue;
            }
            if ("stamp-signer".equals(optionName)) {
                sourceStampFlagFound = true;
                sourceStampSignerParams = ApkSignerTool.processSignerParams(optionsParser);
                continue;
            }
            if ("stamp-lineage".equals(optionName)) {
                File stampLineageFile = new File(optionsParser.getRequiredValue("Stamp Lineage File"));
                sourceStampLineage = ApkSignerTool.getLineageFromInputFile(stampLineageFile);
                continue;
            }
            if ("deterministic-dsa-signing".equals(optionName)) {
                deterministicDsaSigning = optionsParser.getOptionalBooleanValue(false);
                continue;
            }
            if ("append-signature".equals(optionName)) {
                otherSignersSignaturesPreserved = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            throw new ParameterException("Unsupported option: " + optionOriginalForm + ". See --help for supported options.");
        }
        if (!signerParams.isEmpty()) {
            signers.add(signerParams);
        }
        signerParams = null;
        if (!providerParams.isEmpty()) {
            providers.add(providerParams);
        }
        providerParams = null;
        if (signers.isEmpty()) {
            throw new ParameterException("At least one signer must be specified");
        }
        params = optionsParser.getRemainingParams();
        if (inputApk != null) {
            if (params.length > 0) {
                throw new ParameterException("Unexpected parameter(s) after " + optionOriginalForm + ": " + params[0]);
            }
        } else {
            if (params.length < 1) {
                throw new ParameterException("Missing input APK");
            }
            if (params.length > 1) {
                throw new ParameterException("Unexpected parameter(s) after input APK (" + params[1] + ")");
            }
            inputApk = new File(params[0]);
        }
        if (minSdkVersionSpecified && minSdkVersion > maxSdkVersion) {
            throw new ParameterException("Min API Level (" + minSdkVersion + ") > max API Level (" + maxSdkVersion + ")");
        }
        for (ProviderInstallSpec providerInstallSpec : providers) {
            providerInstallSpec.installProvider();
        }
        ApkSigner.SignerConfig sourceStampSignerConfig = null;
        ArrayList<ApkSigner.SignerConfig> signerConfigs = new ArrayList<ApkSigner.SignerConfig>(signers.size());
        int signerNumber = 0;
        try (PasswordRetriever passwordRetriever = new PasswordRetriever();){
            for (SignerParams signer : signers) {
                signer.setName("signer #" + ++signerNumber);
                ApkSigner.SignerConfig signerConfig = ApkSignerTool.getSignerConfig(signer, passwordRetriever, deterministicDsaSigning);
                if (signerConfig == null) {
                    return;
                }
                signerConfigs.add(signerConfig);
            }
            if (sourceStampFlagFound) {
                sourceStampSignerParams.setName("stamp signer");
                sourceStampSignerConfig = ApkSignerTool.getSignerConfig(sourceStampSignerParams, passwordRetriever, deterministicDsaSigning);
                if (sourceStampSignerConfig == null) {
                    return;
                }
            }
        }
        if (outputApk == null) {
            outputApk = inputApk;
        }
        if (inputApk.getCanonicalPath().equals(outputApk.getCanonicalPath())) {
            tmpOutputApk = File.createTempFile("apksigner", ".apk");
            tmpOutputApk.deleteOnExit();
        } else {
            tmpOutputApk = outputApk;
        }
        ApkSigner.Builder apkSignerBuilder = new ApkSigner.Builder(signerConfigs).setInputApk(inputApk).setOutputApk(tmpOutputApk).setOtherSignersSignaturesPreserved(otherSignersSignaturesPreserved).setV1SigningEnabled(v1SigningEnabled).setV2SigningEnabled(v2SigningEnabled).setV3SigningEnabled(v3SigningEnabled).setV4SigningEnabled(v4SigningEnabled).setForceSourceStampOverwrite(forceSourceStampOverwrite).setSourceStampTimestampEnabled(sourceStampTimestampEnabled).setAlignFileSize(alignFileSize).setVerityEnabled(verityEnabled).setV4ErrorReportingEnabled(v4SigningEnabled && v4SigningFlagFound).setDebuggableApkPermitted(debuggableApkPermitted).setSigningCertificateLineage(lineage).setMinSdkVersionForRotation(rotationMinSdkVersion).setRotationTargetsDevRelease(rotationTargetsDevRelease).setAlignmentPreserved(alignmentPreserved).setLibraryPageAlignmentBytes(libPageAlignment);
        if (minSdkVersionSpecified) {
            apkSignerBuilder.setMinSdkVersion(minSdkVersion);
        }
        if (v4SigningEnabled) {
            File outputV4SignatureFile = new File(outputApk.getCanonicalPath() + ".idsig");
            Files.deleteIfExists(outputV4SignatureFile.toPath());
            apkSignerBuilder.setV4SignatureOutputFile(outputV4SignatureFile);
        }
        if (sourceStampSignerConfig != null) {
            apkSignerBuilder.setSourceStampSignerConfig(sourceStampSignerConfig).setSourceStampSigningCertificateLineage(sourceStampLineage);
        }
        ApkSigner apkSigner = apkSignerBuilder.build();
        try {
            apkSigner.sign();
        }
        catch (MinSdkVersionException e) {
            String msg = e.getMessage();
            if (!msg.endsWith(".")) {
                msg = msg + '.';
            }
            throw new MinSdkVersionException("Failed to determine APK's minimum supported platform version. Use --min-sdk-version to override", e);
        }
        if (!tmpOutputApk.getCanonicalPath().equals(outputApk.getCanonicalPath())) {
            Files.move(tmpOutputApk.toPath(), outputApk.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        if (verbose) {
            System.out.println("Signed");
        }
    }

    private static ApkSigner.SignerConfig getSignerConfig(SignerParams signer, PasswordRetriever passwordRetriever, boolean deterministicDsaSigning) {
        String v1SigBasename;
        try {
            signer.loadPrivateKeyAndCerts(passwordRetriever);
        }
        catch (ParameterException e) {
            System.err.println("Failed to load signer \"" + signer.getName() + "\": " + e.getMessage());
            System.exit(2);
            return null;
        }
        catch (Exception e) {
            System.err.println("Failed to load signer \"" + signer.getName() + "\"");
            e.printStackTrace();
            System.exit(2);
            return null;
        }
        if (signer.getV1SigFileBasename() != null) {
            v1SigBasename = signer.getV1SigFileBasename();
        } else if (signer.getKeystoreKeyAlias() != null) {
            v1SigBasename = signer.getKeystoreKeyAlias();
        } else if (signer.getKeyFile() != null) {
            String keyFileName = new File(signer.getKeyFile()).getName();
            int delimiterIndex = keyFileName.indexOf(46);
            v1SigBasename = delimiterIndex == -1 ? keyFileName : keyFileName.substring(0, delimiterIndex);
        } else if (signer.getKmsKeyAlias() != null) {
            v1SigBasename = signer.getKmsKeyAlias();
        } else {
            throw new RuntimeException("Neither KeyStore key alias nor private key file available");
        }
        ApkSigner.SignerConfig.Builder signerConfigBuilder = new ApkSigner.SignerConfig.Builder(v1SigBasename, signer.getKeyConfig(), signer.getCerts(), deterministicDsaSigning);
        SigningCertificateLineage lineage = signer.getSigningCertificateLineage();
        int minSdkVersion = signer.getMinSdkVersion();
        if (minSdkVersion > 0) {
            signerConfigBuilder.setLineageForMinSdkVersion(lineage, minSdkVersion);
        }
        ApkSigner.SignerConfig signerConfig = signerConfigBuilder.build();
        return signerConfig;
    }

    private static void verify(String[] params) throws Exception {
        String signerName;
        ApkVerifier.Result result;
        String optionName;
        if (params.length == 0) {
            ApkSignerTool.printUsage(HELP_PAGE_VERIFY);
            return;
        }
        File inputApk = null;
        int minSdkVersion = 1;
        boolean minSdkVersionSpecified = false;
        int maxSdkVersion = Integer.MAX_VALUE;
        boolean maxSdkVersionSpecified = false;
        boolean printCerts = false;
        boolean printCertsPem = false;
        boolean verbose = false;
        boolean warningsTreatedAsErrors = false;
        boolean verifySourceStamp = false;
        File v4SignatureFile = null;
        OptionsParser optionsParser = new OptionsParser(params);
        String optionOriginalForm = null;
        String sourceCertDigest = null;
        while ((optionName = optionsParser.nextOption()) != null) {
            optionOriginalForm = optionsParser.getOptionOriginalForm();
            if ("min-sdk-version".equals(optionName)) {
                minSdkVersion = optionsParser.getRequiredIntValue("Mininimum API Level");
                minSdkVersionSpecified = true;
                continue;
            }
            if ("max-sdk-version".equals(optionName)) {
                maxSdkVersion = optionsParser.getRequiredIntValue("Maximum API Level");
                maxSdkVersionSpecified = true;
                continue;
            }
            if ("print-certs".equals(optionName)) {
                printCerts = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("print-certs-pem".equals(optionName)) {
                printCertsPem = optionsParser.getOptionalBooleanValue(true);
                if (!printCertsPem || printCerts) continue;
                printCerts = true;
                continue;
            }
            if ("v".equals(optionName) || "verbose".equals(optionName)) {
                verbose = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("Werr".equals(optionName)) {
                warningsTreatedAsErrors = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("help".equals(optionName) || "h".equals(optionName)) {
                ApkSignerTool.printUsage(HELP_PAGE_VERIFY);
                return;
            }
            if ("v4-signature-file".equals(optionName)) {
                v4SignatureFile = new File(optionsParser.getRequiredValue("Input V4 Signature File"));
                continue;
            }
            if ("in".equals(optionName)) {
                inputApk = new File(optionsParser.getRequiredValue("Input APK file"));
                continue;
            }
            if ("verify-source-stamp".equals(optionName)) {
                verifySourceStamp = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("stamp-cert-digest".equals(optionName)) {
                sourceCertDigest = optionsParser.getRequiredValue("Expected source stamp certificate digest");
                continue;
            }
            throw new ParameterException("Unsupported option: " + optionOriginalForm + ". See --help for supported options.");
        }
        params = optionsParser.getRemainingParams();
        if (inputApk != null) {
            if (params.length > 0) {
                throw new ParameterException("Unexpected parameter(s) after " + optionOriginalForm + ": " + params[0]);
            }
        } else {
            if (params.length < 1) {
                throw new ParameterException("Missing APK");
            }
            if (params.length > 1) {
                throw new ParameterException("Unexpected parameter(s) after APK (" + params[1] + ")");
            }
            inputApk = new File(params[0]);
        }
        if (minSdkVersionSpecified && maxSdkVersionSpecified && minSdkVersion > maxSdkVersion) {
            throw new ParameterException("Min API Level (" + minSdkVersion + ") > max API Level (" + maxSdkVersion + ")");
        }
        ApkVerifier.Builder apkVerifierBuilder = new ApkVerifier.Builder(inputApk);
        if (minSdkVersionSpecified) {
            apkVerifierBuilder.setMinCheckedPlatformVersion(minSdkVersion);
        }
        if (maxSdkVersionSpecified) {
            apkVerifierBuilder.setMaxCheckedPlatformVersion(maxSdkVersion);
        }
        if (v4SignatureFile != null) {
            if (!v4SignatureFile.exists()) {
                throw new ParameterException("V4 signature file does not exist: " + v4SignatureFile.getCanonicalPath());
            }
            apkVerifierBuilder.setV4SignatureFile(v4SignatureFile);
        }
        ApkVerifier apkVerifier = apkVerifierBuilder.build();
        try {
            result = verifySourceStamp ? apkVerifier.verifySourceStamp(sourceCertDigest) : apkVerifier.verify();
        }
        catch (MinSdkVersionException e) {
            String msg = e.getMessage();
            if (!msg.endsWith(".")) {
                msg = msg + '.';
            }
            throw new MinSdkVersionException("Failed to determine APK's minimum supported platform version. Use --min-sdk-version to override", e);
        }
        boolean verified = result.isVerified();
        ApkVerifier.Result.SourceStampInfo sourceStampInfo = result.getSourceStampInfo();
        boolean warningsEncountered = false;
        if (verified) {
            List<X509Certificate> signerCerts = result.getSignerCertificates();
            if (verbose) {
                System.out.println("Verifies");
                System.out.println("Verified using v1 scheme (JAR signing): " + result.isVerifiedUsingV1Scheme());
                System.out.println("Verified using v2 scheme (APK Signature Scheme v2): " + result.isVerifiedUsingV2Scheme());
                System.out.println("Verified using v3 scheme (APK Signature Scheme v3): " + result.isVerifiedUsingV3Scheme());
                System.out.println("Verified using v3.1 scheme (APK Signature Scheme v3.1): " + result.isVerifiedUsingV31Scheme());
                System.out.println("Verified using v4 scheme (APK Signature Scheme v4): " + result.isVerifiedUsingV4Scheme());
                System.out.println("Verified for SourceStamp: " + result.isSourceStampVerified());
                if (!verifySourceStamp) {
                    System.out.println("Number of signers: " + signerCerts.size());
                }
            }
            if (printCerts) {
                if (result.isVerifiedUsingV31Scheme()) {
                    for (ApkVerifier.Result.V3SchemeSignerInfo v3SchemeSignerInfo : result.getV31SchemeSigners()) {
                        ApkSignerTool.printCertificate(v3SchemeSignerInfo.getCertificate(), "Signer (minSdkVersion=" + v3SchemeSignerInfo.getMinSdkVersion() + (v3SchemeSignerInfo.getRotationTargetsDevRelease() ? " (dev release=true)" : "") + ", maxSdkVersion=" + v3SchemeSignerInfo.getMaxSdkVersion() + ")", verbose, printCertsPem);
                    }
                    for (ApkVerifier.Result.V3SchemeSignerInfo v3SchemeSignerInfo : result.getV3SchemeSigners()) {
                        ApkSignerTool.printCertificate(v3SchemeSignerInfo.getCertificate(), "Signer (minSdkVersion=" + v3SchemeSignerInfo.getMinSdkVersion() + ", maxSdkVersion=" + v3SchemeSignerInfo.getMaxSdkVersion() + ")", verbose, printCertsPem);
                    }
                } else {
                    int signerNumber = 0;
                    Iterator iterator = signerCerts.iterator();
                    while (iterator.hasNext()) {
                        X509Certificate signerCert = (X509Certificate)iterator.next();
                        ApkSignerTool.printCertificate(signerCert, "Signer #" + ++signerNumber, verbose, printCertsPem);
                    }
                }
                if (sourceStampInfo != null) {
                    ApkSignerTool.printCertificate(sourceStampInfo.getCertificate(), "Source Stamp Signer", verbose, printCertsPem);
                }
            }
            if (sourceStampInfo != null && verbose) {
                System.out.println("Source Stamp Timestamp: " + sourceStampInfo.getTimestampEpochSeconds());
            }
        } else {
            System.err.println("DOES NOT VERIFY");
        }
        for (ApkVerifier.IssueWithParams error : result.getErrors()) {
            System.err.println("ERROR: " + error);
        }
        PrintStream warningsOut = warningsTreatedAsErrors ? System.err : System.out;
        for (ApkVerifier.IssueWithParams issueWithParams : result.getWarnings()) {
            warningsEncountered = true;
            warningsOut.println("WARNING: " + issueWithParams);
        }
        for (ApkVerifier.Result.V1SchemeSignerInfo v1SchemeSignerInfo : result.getV1SchemeSigners()) {
            signerName = v1SchemeSignerInfo.getName();
            for (ApkVerifier.IssueWithParams error : v1SchemeSignerInfo.getErrors()) {
                System.err.println("ERROR: JAR signer " + signerName + ": " + error);
            }
            for (ApkVerifier.IssueWithParams warning : v1SchemeSignerInfo.getWarnings()) {
                warningsEncountered = true;
                warningsOut.println("WARNING: JAR signer " + signerName + ": " + warning);
            }
        }
        for (ApkVerifier.Result.V2SchemeSignerInfo v2SchemeSignerInfo : result.getV2SchemeSigners()) {
            signerName = "signer #" + (v2SchemeSignerInfo.getIndex() + 1);
            for (ApkVerifier.IssueWithParams error : v2SchemeSignerInfo.getErrors()) {
                System.err.println("ERROR: APK Signature Scheme v2 " + signerName + ": " + error);
            }
            for (ApkVerifier.IssueWithParams warning : v2SchemeSignerInfo.getWarnings()) {
                warningsEncountered = true;
                warningsOut.println("WARNING: APK Signature Scheme v2 " + signerName + ": " + warning);
            }
        }
        for (ApkVerifier.Result.V3SchemeSignerInfo v3SchemeSignerInfo : result.getV3SchemeSigners()) {
            signerName = "signer #" + (v3SchemeSignerInfo.getIndex() + 1);
            for (ApkVerifier.IssueWithParams error : v3SchemeSignerInfo.getErrors()) {
                System.err.println("ERROR: APK Signature Scheme v3 " + signerName + ": " + error);
            }
            for (ApkVerifier.IssueWithParams warning : v3SchemeSignerInfo.getWarnings()) {
                warningsEncountered = true;
                warningsOut.println("WARNING: APK Signature Scheme v3 " + signerName + ": " + warning);
            }
        }
        for (ApkVerifier.Result.V3SchemeSignerInfo v3SchemeSignerInfo : result.getV31SchemeSigners()) {
            signerName = "signer #" + (v3SchemeSignerInfo.getIndex() + 1) + "(minSdkVersion=" + v3SchemeSignerInfo.getMinSdkVersion() + ", maxSdkVersion=" + v3SchemeSignerInfo.getMaxSdkVersion() + ")";
            for (ApkVerifier.IssueWithParams error : v3SchemeSignerInfo.getErrors()) {
                System.err.println("ERROR: APK Signature Scheme v3.1 " + signerName + ": " + error);
            }
            for (ApkVerifier.IssueWithParams warning : v3SchemeSignerInfo.getWarnings()) {
                warningsEncountered = true;
                warningsOut.println("WARNING: APK Signature Scheme v3.1 " + signerName + ": " + warning);
            }
        }
        if (sourceStampInfo != null) {
            for (ApkVerifier.IssueWithParams issueWithParams : sourceStampInfo.getErrors()) {
                System.err.println("ERROR: SourceStamp: " + issueWithParams);
            }
            for (ApkVerifier.IssueWithParams issueWithParams : sourceStampInfo.getWarnings()) {
                warningsOut.println("WARNING: SourceStamp: " + issueWithParams);
            }
            for (ApkVerifier.IssueWithParams issueWithParams : sourceStampInfo.getInfoMessages()) {
                System.out.println("INFO: SourceStamp: " + issueWithParams);
            }
        }
        if (!verified) {
            System.exit(1);
            return;
        }
        if (warningsTreatedAsErrors && warningsEncountered) {
            System.exit(1);
            return;
        }
    }

    private static void rotate(String[] params) throws Exception {
        String optionName;
        if (params.length == 0) {
            ApkSignerTool.printUsage(HELP_PAGE_ROTATE);
            return;
        }
        File outputKeyLineage = null;
        File inputKeyLineage = null;
        boolean verbose = false;
        SignerParams oldSignerParams = null;
        SignerParams newSignerParams = null;
        int minSdkVersion = 0;
        ArrayList<ProviderInstallSpec> providers = new ArrayList<ProviderInstallSpec>();
        ProviderInstallSpec providerParams = new ProviderInstallSpec();
        OptionsParser optionsParser = new OptionsParser(params);
        String optionOriginalForm = null;
        while ((optionName = optionsParser.nextOption()) != null) {
            optionOriginalForm = optionsParser.getOptionOriginalForm();
            if ("help".equals(optionName) || "h".equals(optionName)) {
                ApkSignerTool.printUsage(HELP_PAGE_ROTATE);
                return;
            }
            if ("out".equals(optionName)) {
                outputKeyLineage = new File(optionsParser.getRequiredValue("Output file name"));
                continue;
            }
            if ("in".equals(optionName)) {
                inputKeyLineage = new File(optionsParser.getRequiredValue("Input file name"));
                continue;
            }
            if ("old-signer".equals(optionName)) {
                oldSignerParams = ApkSignerTool.processSignerParams(optionsParser);
                continue;
            }
            if ("new-signer".equals(optionName)) {
                newSignerParams = ApkSignerTool.processSignerParams(optionsParser);
                continue;
            }
            if ("min-sdk-version".equals(optionName)) {
                minSdkVersion = optionsParser.getRequiredIntValue("Mininimum API Level");
                continue;
            }
            if ("v".equals(optionName) || "verbose".equals(optionName)) {
                verbose = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("next-provider".equals(optionName)) {
                if (providerParams.isEmpty()) continue;
                providers.add(providerParams);
                providerParams = new ProviderInstallSpec();
                continue;
            }
            if ("provider-class".equals(optionName)) {
                providerParams.className = optionsParser.getRequiredValue("JCA Provider class name");
                continue;
            }
            if ("provider-arg".equals(optionName)) {
                providerParams.constructorParam = optionsParser.getRequiredValue("JCA Provider constructor argument");
                continue;
            }
            if ("provider-pos".equals(optionName)) {
                providerParams.position = optionsParser.getRequiredIntValue("JCA Provider position");
                continue;
            }
            throw new ParameterException("Unsupported option: " + optionOriginalForm + ". See --help for supported options.");
        }
        if (!providerParams.isEmpty()) {
            providers.add(providerParams);
        }
        providerParams = null;
        if (oldSignerParams.isEmpty()) {
            throw new ParameterException("Signer parameters for old signer not present");
        }
        if (newSignerParams.isEmpty()) {
            throw new ParameterException("Signer parameters for new signer not present");
        }
        if (outputKeyLineage == null) {
            throw new ParameterException("Output lineage file parameter not present");
        }
        params = optionsParser.getRemainingParams();
        if (params.length > 0) {
            throw new ParameterException("Unexpected parameter(s) after " + optionOriginalForm + ": " + params[0]);
        }
        for (ProviderInstallSpec providerInstallSpec : providers) {
            providerInstallSpec.installProvider();
        }
        try (PasswordRetriever passwordRetriever = new PasswordRetriever();){
            SigningCertificateLineage lineage;
            oldSignerParams.setName("old signer");
            ApkSignerTool.loadPrivateKeyAndCerts(oldSignerParams, passwordRetriever);
            SigningCertificateLineage.SignerConfig oldSignerConfig = new SigningCertificateLineage.SignerConfig.Builder(oldSignerParams.getKeyConfig(), oldSignerParams.getCerts().get(0)).build();
            newSignerParams.setName("new signer");
            ApkSignerTool.loadPrivateKeyAndCerts(newSignerParams, passwordRetriever);
            SigningCertificateLineage.SignerConfig newSignerConfig = new SigningCertificateLineage.SignerConfig.Builder(newSignerParams.getKeyConfig(), newSignerParams.getCerts().get(0)).build();
            if (inputKeyLineage != null) {
                lineage = ApkSignerTool.getLineageFromInputFile(inputKeyLineage);
                lineage.updateSignerCapabilities(oldSignerConfig, oldSignerParams.getSignerCapabilitiesBuilder().build());
                lineage = lineage.spawnDescendant(oldSignerConfig, newSignerConfig, newSignerParams.getSignerCapabilitiesBuilder().build());
            } else {
                lineage = new SigningCertificateLineage.Builder(oldSignerConfig, newSignerConfig).setMinSdkVersion(minSdkVersion).setOriginalCapabilities(oldSignerParams.getSignerCapabilitiesBuilder().build()).setNewCapabilities(newSignerParams.getSignerCapabilitiesBuilder().build()).build();
            }
            lineage.writeToFile(outputKeyLineage);
        }
        if (verbose) {
            System.out.println("Rotation entry generated.");
        }
    }

    public static void lineage(String[] params) throws Exception {
        int i;
        String optionName;
        if (params.length == 0) {
            ApkSignerTool.printUsage(HELP_PAGE_LINEAGE);
            return;
        }
        boolean verbose = false;
        boolean printCerts = false;
        boolean printCertsPem = false;
        boolean lineageUpdated = false;
        File inputKeyLineage = null;
        File outputKeyLineage = null;
        OptionsParser optionsParser = new OptionsParser(params);
        ArrayList<SignerParams> signers = new ArrayList<SignerParams>(1);
        while ((optionName = optionsParser.nextOption()) != null) {
            if ("help".equals(optionName) || "h".equals(optionName)) {
                ApkSignerTool.printUsage(HELP_PAGE_LINEAGE);
                return;
            }
            if ("in".equals(optionName)) {
                inputKeyLineage = new File(optionsParser.getRequiredValue("Input file name"));
                continue;
            }
            if ("out".equals(optionName)) {
                outputKeyLineage = new File(optionsParser.getRequiredValue("Output file name"));
                continue;
            }
            if ("signer".equals(optionName)) {
                SignerParams signerParams = ApkSignerTool.processSignerParams(optionsParser);
                signers.add(signerParams);
                continue;
            }
            if ("v".equals(optionName) || "verbose".equals(optionName)) {
                verbose = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("print-certs".equals(optionName)) {
                printCerts = optionsParser.getOptionalBooleanValue(true);
                continue;
            }
            if ("print-certs-pem".equals(optionName)) {
                printCertsPem = optionsParser.getOptionalBooleanValue(true);
                if (!printCertsPem || printCerts) continue;
                printCerts = true;
                continue;
            }
            throw new ParameterException("Unsupported option: " + optionsParser.getOptionOriginalForm() + ". See --help for supported options.");
        }
        if (inputKeyLineage == null) {
            throw new ParameterException("Input lineage file parameter not present");
        }
        SigningCertificateLineage lineage = ApkSignerTool.getLineageFromInputFile(inputKeyLineage);
        try (PasswordRetriever passwordRetriever = new PasswordRetriever();){
            for (i = 0; i < signers.size(); ++i) {
                SignerParams signerParams = (SignerParams)signers.get(i);
                signerParams.setName("signer #" + (i + 1));
                ApkSignerTool.loadPrivateKeyAndCerts(signerParams, passwordRetriever);
                SigningCertificateLineage.SignerConfig signerConfig = new SigningCertificateLineage.SignerConfig.Builder(signerParams.getKeyConfig(), signerParams.getCerts().get(0)).build();
                try {
                    SigningCertificateLineage.SignerCapabilities origCapabilities = lineage.getSignerCapabilities(signerConfig);
                    lineage.updateSignerCapabilities(signerConfig, signerParams.getSignerCapabilitiesBuilder().build());
                    SigningCertificateLineage.SignerCapabilities newCapabilities = lineage.getSignerCapabilities(signerConfig);
                    if (origCapabilities.equals(newCapabilities)) {
                        if (!verbose) continue;
                        System.out.println("The provided signer capabilities for " + signerParams.getName() + " are unchanged.");
                        continue;
                    }
                    lineageUpdated = true;
                    if (!verbose) continue;
                    System.out.println("Updated signer capabilities for " + signerParams.getName() + ".");
                    continue;
                }
                catch (IllegalArgumentException e) {
                    throw new ParameterException("The signer " + signerParams.getName() + " was not found in the specified lineage.");
                }
            }
        }
        if (printCerts) {
            List<X509Certificate> signingCerts = lineage.getCertificatesInLineage();
            for (i = 0; i < signingCerts.size(); ++i) {
                X509Certificate signerCert = signingCerts.get(i);
                SigningCertificateLineage.SignerCapabilities signerCapabilities = lineage.getSignerCapabilities(signerCert);
                ApkSignerTool.printCertificate(signerCert, "Signer #" + (i + 1) + " in lineage", verbose, printCertsPem);
                ApkSignerTool.printCapabilities(signerCapabilities);
            }
        }
        if (lineageUpdated) {
            if (outputKeyLineage != null) {
                lineage.writeToFile(outputKeyLineage);
                if (verbose) {
                    System.out.println("Updated lineage saved to " + outputKeyLineage + ".");
                }
            } else {
                throw new ParameterException("The lineage was modified but an output file for the lineage was not specified");
            }
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static SigningCertificateLineage getLineageFromInputFile(File inputLineageFile) throws ParameterException {
        try (RandomAccessFile f = new RandomAccessFile(inputLineageFile, "r");){
            if (f.length() < 4L) {
                throw new ParameterException("The input file is not a valid lineage file.");
            }
            DataSource apk = DataSources.asDataSource(f);
            int magicValue = apk.getByteBuffer(0L, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
            if (magicValue == 1056913873) {
                SigningCertificateLineage signingCertificateLineage = SigningCertificateLineage.readFromFile(inputLineageFile);
                return signingCertificateLineage;
            }
            if (magicValue != 67324752) throw new ParameterException("The input file is not a valid lineage file.");
            SigningCertificateLineage signingCertificateLineage = SigningCertificateLineage.readFromApkFile(inputLineageFile);
            return signingCertificateLineage;
        }
        catch (ApkFormatException | IOException | IllegalArgumentException e) {
            throw new ParameterException(e.getMessage());
        }
    }

    private static SignerParams processSignerParams(OptionsParser optionsParser) throws OptionsParser.OptionsException, ParameterException {
        String optionName;
        SignerParams signerParams = new SignerParams();
        while ((optionName = optionsParser.nextOption()) != null) {
            if ("ks".equals(optionName)) {
                signerParams.setKeystoreFile(optionsParser.getRequiredValue("KeyStore file"));
                continue;
            }
            if ("ks-key-alias".equals(optionName)) {
                signerParams.setKeystoreKeyAlias(optionsParser.getRequiredValue("KeyStore key alias"));
                continue;
            }
            if ("ks-pass".equals(optionName)) {
                signerParams.setKeystorePasswordSpec(optionsParser.getRequiredValue("KeyStore password"));
                continue;
            }
            if ("key-pass".equals(optionName)) {
                signerParams.setKeyPasswordSpec(optionsParser.getRequiredValue("Key password"));
                continue;
            }
            if ("pass-encoding".equals(optionName)) {
                String charsetName = optionsParser.getRequiredValue("Password character encoding");
                try {
                    signerParams.setPasswordCharset(PasswordRetriever.getCharsetByName(charsetName));
                    continue;
                }
                catch (IllegalArgumentException e) {
                    throw new ParameterException("Unsupported password character encoding requested using --pass-encoding: " + charsetName);
                }
            }
            if ("ks-type".equals(optionName)) {
                signerParams.setKeystoreType(optionsParser.getRequiredValue("KeyStore type"));
                continue;
            }
            if ("ks-provider-name".equals(optionName)) {
                signerParams.setKeystoreProviderName(optionsParser.getRequiredValue("JCA KeyStore Provider name"));
                continue;
            }
            if ("ks-provider-class".equals(optionName)) {
                signerParams.setKeystoreProviderClass(optionsParser.getRequiredValue("JCA KeyStore Provider class name"));
                continue;
            }
            if ("ks-provider-arg".equals(optionName)) {
                signerParams.setKeystoreProviderArg(optionsParser.getRequiredValue("JCA KeyStore Provider constructor argument"));
                continue;
            }
            if ("kms-type".equals(optionName)) {
                signerParams.setKmsType(optionsParser.getRequiredValue("KMS Type"));
                continue;
            }
            if ("kms-key-alias".equals(optionName)) {
                signerParams.setKmsKeyAlias(optionsParser.getRequiredValue("KMS Key Alias"));
                continue;
            }
            if ("key".equals(optionName)) {
                signerParams.setKeyFile(optionsParser.getRequiredValue("Private key file"));
                continue;
            }
            if ("cert".equals(optionName)) {
                signerParams.setCertFile(optionsParser.getRequiredValue("Certificate file"));
                continue;
            }
            if ("set-installed-data".equals(optionName)) {
                signerParams.getSignerCapabilitiesBuilder().setInstalledData(optionsParser.getOptionalBooleanValue(true));
                continue;
            }
            if ("set-shared-uid".equals(optionName)) {
                signerParams.getSignerCapabilitiesBuilder().setSharedUid(optionsParser.getOptionalBooleanValue(true));
                continue;
            }
            if ("set-permission".equals(optionName)) {
                signerParams.getSignerCapabilitiesBuilder().setPermission(optionsParser.getOptionalBooleanValue(true));
                continue;
            }
            if ("set-rollback".equals(optionName)) {
                signerParams.getSignerCapabilitiesBuilder().setRollback(optionsParser.getOptionalBooleanValue(true));
                continue;
            }
            if ("set-auth".equals(optionName)) {
                signerParams.getSignerCapabilitiesBuilder().setAuth(optionsParser.getOptionalBooleanValue(true));
                continue;
            }
            optionsParser.putOption();
            break;
        }
        if (signerParams.isEmpty()) {
            throw new ParameterException("Signer specified without arguments");
        }
        return signerParams;
    }

    private static void printUsage(String page) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(ApkSignerTool.class.getResourceAsStream(page), StandardCharsets.UTF_8));){
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read " + page + " resource");
        }
    }

    public static void printCertificate(X509Certificate cert, String name, boolean verbose) throws NoSuchAlgorithmException, CertificateEncodingException {
        ApkSignerTool.printCertificate(cert, name, verbose, false);
    }

    public static void printCertificate(X509Certificate cert, String name, boolean verbose, boolean pemOutput) throws NoSuchAlgorithmException, CertificateEncodingException {
        if (cert == null) {
            throw new NullPointerException("cert == null");
        }
        if (sha256 == null || sha1 == null || md5 == null) {
            sha256 = MessageDigest.getInstance("SHA-256");
            sha1 = MessageDigest.getInstance("SHA-1");
            md5 = MessageDigest.getInstance("MD5");
        }
        System.out.println(name + " certificate DN: " + cert.getSubjectDN());
        byte[] encodedCert = cert.getEncoded();
        System.out.println(name + " certificate SHA-256 digest: " + HexEncoding.encode(sha256.digest(encodedCert)));
        System.out.println(name + " certificate SHA-1 digest: " + HexEncoding.encode(sha1.digest(encodedCert)));
        System.out.println(name + " certificate MD5 digest: " + HexEncoding.encode(md5.digest(encodedCert)));
        if (verbose) {
            DSAParams dsaParams;
            PublicKey publicKey = cert.getPublicKey();
            System.out.println(name + " key algorithm: " + publicKey.getAlgorithm());
            int keySize = -1;
            if (publicKey instanceof RSAKey) {
                keySize = ((RSAKey)((Object)publicKey)).getModulus().bitLength();
            } else if (publicKey instanceof ECKey) {
                keySize = ((ECKey)((Object)publicKey)).getParams().getOrder().bitLength();
            } else if (publicKey instanceof DSAKey && (dsaParams = ((DSAKey)((Object)publicKey)).getParams()) != null) {
                keySize = dsaParams.getP().bitLength();
            }
            System.out.println(name + " key size (bits): " + (keySize != -1 ? String.valueOf(keySize) : "n/a"));
            byte[] encodedKey = publicKey.getEncoded();
            System.out.println(name + " public key SHA-256 digest: " + HexEncoding.encode(sha256.digest(encodedKey)));
            System.out.println(name + " public key SHA-1 digest: " + HexEncoding.encode(sha1.digest(encodedKey)));
            System.out.println(name + " public key MD5 digest: " + HexEncoding.encode(md5.digest(encodedKey)));
        }
        if (pemOutput) {
            System.out.println(BEGIN_CERTIFICATE);
            int lineWidth = 64;
            String pemEncodedCert = Base64.getEncoder().encodeToString(cert.getEncoded());
            for (int i = 0; i < pemEncodedCert.length(); i += 64) {
                System.out.println(pemEncodedCert.substring(i, i + 64 > pemEncodedCert.length() ? pemEncodedCert.length() : i + 64));
            }
            System.out.println(END_CERTIFICATE);
        }
    }

    public static void printCapabilities(SigningCertificateLineage.SignerCapabilities capabilities) {
        System.out.println("Has installed data capability: " + capabilities.hasInstalledData());
        System.out.println("Has shared UID capability    : " + capabilities.hasSharedUid());
        System.out.println("Has permission capability    : " + capabilities.hasPermission());
        System.out.println("Has rollback capability      : " + capabilities.hasRollback());
        System.out.println("Has auth capability          : " + capabilities.hasAuth());
    }

    private static void loadPrivateKeyAndCerts(SignerParams params, PasswordRetriever passwordRetriever) throws ParameterException {
        block7: {
            try {
                params.loadPrivateKeyAndCerts(passwordRetriever);
                if (params.getKeystoreKeyAlias() != null) {
                    params.setName(params.getKeystoreKeyAlias());
                    break block7;
                }
                if (params.getKeyFile() != null) {
                    String keyFileName = new File(params.getKeyFile()).getName();
                    int delimiterIndex = keyFileName.indexOf(46);
                    if (delimiterIndex == -1) {
                        params.setName(keyFileName);
                    } else {
                        params.setName(keyFileName.substring(0, delimiterIndex));
                    }
                    break block7;
                }
                throw new RuntimeException("Neither KeyStore key alias nor private key file available for " + params.getName());
            }
            catch (ParameterException e) {
                throw new ParameterException("Failed to load signer \"" + params.getName() + "\":" + e.getMessage());
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new ParameterException("Failed to load signer \"" + params.getName() + "\"");
            }
        }
    }

    private static class ProviderInstallSpec {
        String className;
        String constructorParam;
        Integer position;

        private ProviderInstallSpec() {
        }

        private boolean isEmpty() {
            return this.className == null && this.constructorParam == null && this.position == null;
        }

        private void installProvider() throws Exception {
            Provider provider;
            if (this.className == null) {
                throw new ParameterException("JCA Provider class name (--provider-class) must be specified");
            }
            Class<?> providerClass = Class.forName(this.className);
            if (!Provider.class.isAssignableFrom(providerClass)) {
                throw new ParameterException("JCA Provider class " + providerClass + " not subclass of " + Provider.class.getName());
            }
            if (this.constructorParam != null) {
                try {
                    provider = (Provider)providerClass.getConstructor(String.class).newInstance(this.constructorParam);
                }
                catch (NoSuchMethodException e) {
                    provider = (Provider)providerClass.getConstructor(new Class[0]).newInstance(new Object[0]);
                    provider = (Provider)providerClass.getMethod("configure", String.class).invoke(provider, this.constructorParam);
                }
            } else {
                provider = (Provider)providerClass.getConstructor(new Class[0]).newInstance(new Object[0]);
            }
            if (this.position == null) {
                Security.addProvider(provider);
            } else {
                Security.insertProviderAt(provider, this.position);
            }
        }
    }
}

