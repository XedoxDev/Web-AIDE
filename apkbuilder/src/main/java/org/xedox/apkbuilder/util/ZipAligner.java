package org.xedox.apkbuilder.util;

public class ZipAligner {
    static {
        System.loadLibrary("zipalign");
    }

    public static native boolean align(String srcZip, String destZip, int alignLevel, boolean pageAlignSharedLibs);

    public static native boolean isAligned(String srcZip, int alignLevel, boolean pageAlignSharedLibs);
}
