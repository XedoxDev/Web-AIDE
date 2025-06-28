package org.xedox.apkbuilder.util;

import java.io.PrintStream;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskManager {

    private final PrintStream out;
    private boolean verbose = false;
    private final Stack<Long> taskStartTimes = new Stack<>();
    private final AtomicInteger taskDepth = new AtomicInteger(0);
    private int successCount = 0;
    private int failureCount = 0;
    private int warningCount = 0;
    private long globalStartTime = 0;

    public TaskManager(PrintStream out) {
        this.out = out;
    }

    @FunctionalInterface
    public interface Task {
        void run() throws Exception;
    }

    public void start() {
        globalStartTime = System.currentTimeMillis();
        reset();
    }

    public void reset() {
        successCount = 0;
        failureCount = 0;
        warningCount = 0;
        taskDepth.set(0);
        taskStartTimes.clear();
    }

    public void task(String name, Task task) throws Exception {
        long startTime = System.currentTimeMillis();
        taskStartTimes.push(startTime);

        printTaskHeader(name);
        taskDepth.incrementAndGet();

        try {
            task.run();
            successCount++;
            printTaskSuccess(name, startTime);
        } catch (Exception e) {
            failureCount++;
            printTaskFailure(name, startTime);
            throw e;
        } finally {
            taskDepth.decrementAndGet();
            if (!taskStartTimes.isEmpty()) {
                taskStartTimes.pop();
            }
        }
    }

    public void log(String message) {
        out.println(indent() + message);
    }

    public void debug(String message) {
        if (verbose) {
            out.println(indent() + "[DEBUG] " + message);
        }
    }

    public void error(String message) {
        out.println(indent() + "[ERROR] " + message);
    }

    public void warn(String message) {
        warningCount++;
        out.println(indent() + "[WARN] " + message);
    }

    public void error(String message, Throwable e) {
        error(message);
        if (e != null) {
            e.printStackTrace(out);
        }
    }

    public String getStatistics() {
        long totalTime = globalStartTime > 0 ? System.currentTimeMillis() - globalStartTime : 0;
        return String.format(
                "Tasks: %d successful, %d failed, %d warnings | Total time: %dms",
                successCount, failureCount, warningCount, totalTime);
    }

    public void printStatistics() {
        out.println("\n=== Execution Summary ===");
        out.println(getStatistics());
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public PrintStream getPrintStream() {
        return this.out;
    }

    public void printTaskHeader(String name) {
        out.println(indent() + ":Task " + name);
    }

    public void printTaskSuccess(String name, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        out.println(indent() + "Completed: " + name + " (" + duration + "ms)");
    }

    public void printTaskFailure(String name, long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        out.println(indent() + "Failed: " + name + " (" + duration + "ms)");
    }

    private String indent() {
        if (taskDepth.get() <= 0) {
            return "";
        }
        return "   ".repeat(taskDepth.get());
    }
}
