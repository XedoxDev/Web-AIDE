package org.xedox.javac;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.io.File;

public class JavacOptionsBuilder extends OptionsBuilder {

    private String source;
    private String target;
    private String release;
    private String destination;
    private String sourcepath;
    private final List<String> classpath = new ArrayList<>();
    private final List<String> options = new ArrayList<>();
    private final List<String> sourceFiles = new ArrayList<>();

    public static JavacOptionsBuilder create() {
        return new JavacOptionsBuilder();
    }

    public JavacOptionsBuilder source(String sourceVersion) {
        this.source = Objects.requireNonNull(sourceVersion, "Source version cannot be null");
        return this;
    }

    public JavacOptionsBuilder target(String targetVersion) {
        this.target = Objects.requireNonNull(targetVersion, "Target version cannot be null");
        return this;
    }

    public JavacOptionsBuilder release(String releaseVersion) {
        this.release = Objects.requireNonNull(releaseVersion, "Release version cannot be null");
        return this;
    }

    public JavacOptionsBuilder destination(String outputDir) {
        this.destination = Objects.requireNonNull(outputDir, "Output directory cannot be null");
        return this;
    }

    public JavacOptionsBuilder destination(Path outputDir) {
        this.destination =
                Objects.requireNonNull(outputDir, "Output directory cannot be null").toString();
        return this;
    }

    public JavacOptionsBuilder sourcepath(String sourcepath) {
        this.sourcepath = Objects.requireNonNull(sourcepath, "Sourcepath cannot be null");
        return this;
    }

    public JavacOptionsBuilder sourcepath(Path sourcepath) {
        this.sourcepath =
                Objects.requireNonNull(sourcepath, "Sourcepath cannot be null").toString();
        return this;
    }

    public JavacOptionsBuilder classpath(String classpathEntry) {
        this.classpath.add(
                Objects.requireNonNull(classpathEntry, "Classpath entry cannot be null"));
        return this;
    }

    public JavacOptionsBuilder classpath(Path classpathEntry) {
        this.classpath.add(
                Objects.requireNonNull(classpathEntry, "Classpath entry cannot be null")
                        .toString());
        return this;
    }

    public JavacOptionsBuilder classpath(List<String> classpathEntries) {
        this.classpath.addAll(
                Objects.requireNonNull(classpathEntries, "Classpath entries cannot be null"));
        return this;
    }

    public JavacOptionsBuilder option(String... option) {
        for (String opt : option) {
            this.options.add(Objects.requireNonNull(opt, "Option cannot be null"));
        }
        return this;
    }

    public JavacOptionsBuilder options(List<String> options) {
        this.options.addAll(Objects.requireNonNull(options, "Options list cannot be null"));
        return this;
    }

    public JavacOptionsBuilder addSrc(String... sources) {
        for (String src : Objects.requireNonNull(sources, "Sources cannot be null")) {
            this.sourceFiles.add(src);
        }
        return this;
    }

    public JavacOptionsBuilder addSrc(List<String> sources) {
        this.sourceFiles.addAll(Objects.requireNonNull(sources, "Sources list cannot be null"));
        return this;
    }

    @Override
    public String[] build() {
        args.clear();

        if (release != null) {
            arg("--release", release);
        } else {
            if (source != null) {
                arg("-source", source);
            }
            if (target != null) {
                arg("-target", target);
            }
        }

        if (destination != null) {
            arg("-d", destination);
        }

        if (sourcepath != null) {
            arg("-sourcepath", sourcepath);
        }

        if (!classpath.isEmpty()) {
            arg("-classpath", String.join(File.pathSeparator, classpath));
        }

        arg(options);
        arg(sourceFiles);

        return super.build();
    }

    @Override
    public String buildCmd() {
        return String.join(" ", build());
    }
}
