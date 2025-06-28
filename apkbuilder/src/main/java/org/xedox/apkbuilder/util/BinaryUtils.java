package org.xedox.apkbuilder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class BinaryUtils {
    public static String execute(String[] command) throws Exception {
        Process process = null;
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        process = pb.start();

        StringBuilder output = new StringBuilder();
        try (InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out: " + String.join(" ", command));
        }
        return output.toString();
    }

    public static void setExecutable(File file) throws Exception {
        if (!file.exists()) {
            throw new BuildException("File not found: " + file);
        }

        if (!file.setExecutable(true, true)) {
            Runtime.getRuntime().exec(new String[] {"chmod", "777", file.getAbsolutePath()});
        }
    }
}
