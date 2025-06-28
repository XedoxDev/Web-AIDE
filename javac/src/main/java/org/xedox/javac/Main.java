package org.xedox.javac;

public class Main {
    public static void main(String... args) {
    	JavaCompiler javac = new JavaCompiler();
        javac.compile(args);
    }
}
