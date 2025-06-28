package org.xedox.javac;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.batch.Main;

public final class JavaCompiler {
    
    private PrintWriter outputWriter;
    private PrintWriter errorWriter;
    private boolean isSystemExitEnabled;
    private Map<String, String> errorCodes;
    private CompilationProgress progressMonitor;

    public JavaCompiler() {
        this(new PrintWriter(System.out), new PrintWriter(System.err));
    }

    public JavaCompiler(PrintWriter outputWriter, PrintWriter errorWriter) {
        this(outputWriter, errorWriter, false, null, null);
    }

    public JavaCompiler(
            PrintWriter outputWriter,
            PrintWriter errorWriter,
            boolean isSystemExitEnabled,
            Map<String, String> errorCodes,
            CompilationProgress progressMonitor) {
        this.outputWriter = outputWriter;
        this.errorWriter = errorWriter;
        this.isSystemExitEnabled = isSystemExitEnabled;
        this.errorCodes = errorCodes;
        this.progressMonitor = progressMonitor;
    }

    public boolean compile(String... options) {
        Main compiler =
                new Main(outputWriter, errorWriter, isSystemExitEnabled, errorCodes, progressMonitor);
        return compiler.compile(options);
    }

    public boolean compile(List<String> options) {
        return compile(options.toArray(new String[0]));
    }

    public boolean compile(OptionsBuilder builder) {
        return compile(builder.build());
    }

    public boolean compile(List<File> sourceFiles, List<String> additionalOptions) {
        List<String> options = new ArrayList<>(additionalOptions);
        sourceFiles.forEach(file -> options.add(file.getPath()));
        return compile(options);
    }

    public PrintWriter getOutputWriter() {
        return this.outputWriter;
    }

    public void setOutputWriter(PrintWriter outputWriter) {
        this.outputWriter = outputWriter;
    }

    public PrintWriter getErrorWriter() {
        return this.errorWriter;
    }

    public void setErrorWriter(PrintWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    public boolean isSystemExitEnabled() {
        return this.isSystemExitEnabled;
    }

    public void setSystemExitEnabled(boolean isSystemExitEnabled) {
        this.isSystemExitEnabled = isSystemExitEnabled;
    }

    public Map<String, String> getErrorCodes() {
        return this.errorCodes;
    }

    public void setErrorCodes(Map<String, String> errorCodes) {
        this.errorCodes = errorCodes;
    }

    public CompilationProgress getProgressMonitor() {
        return this.progressMonitor;
    }

    public void setProgressMonitor(CompilationProgress progressMonitor) {
        this.progressMonitor = progressMonitor;
    }
}