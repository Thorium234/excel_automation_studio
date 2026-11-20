package com.excelstudio.app;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.excelstudio.config.AppConfig;
import com.excelstudio.excel.ExcelTaskEngine;
import com.excelstudio.excel.PoiTaskValidator;
import com.excelstudio.excel.TaskGenerator;
import com.excelstudio.excel.WorkbookTaskExecutor;
import com.excelstudio.model.TaskResult;
import com.excelstudio.service.ExecutionLog;
import com.excelstudio.service.ReportService;
import com.excelstudio.service.WorkbookService;
import com.excelstudio.session.SessionConfig;
import com.excelstudio.session.SessionManager;
import com.excelstudio.session.SessionState;

/**
 * Wires the application's services together. This is the only shared state the
 * JavaFX controllers use; the underlying modules stay independent of JavaFX.
 */
public final class AppServices {
    private static volatile AppServices instance;

    private final AppConfig config;
    private final WorkbookService workbooks = new WorkbookService();
    private final TaskGenerator generator = new TaskGenerator();
    private final ExcelTaskEngine engine;
    private final ReportService reports;
    private final ExecutionLog log = new ExecutionLog();
    private final ExecutorService workers;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private volatile SessionManager session;
    private volatile String currentTask = "None";
    private volatile Path currentWorkbook;

    private AppServices(AppConfig config) {
        this.config = Objects.requireNonNull(config, "config");
        this.engine = new ExcelTaskEngine(generator, new WorkbookTaskExecutor(workbooks),
                new PoiTaskValidator(workbooks));
        this.reports = new ReportService(config.getReportsDirectory());
        this.workers = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "excel-studio-worker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static AppServices initialize(AppConfig config) {
        AppServices local = instance;
        if (local == null) {
            synchronized (AppServices.class) {
                local = instance;
                if (local == null) {
                    local = new AppServices(config);
                    instance = local;
                }
            }
        }
        return local;
    }

    public static AppServices get() {
        AppServices local = instance;
        if (local == null) {
            local = initialize(AppConfig.loadDefault());
        }
        return local;
    }

    public AppConfig config() {
        return config;
    }

    public WorkbookService workbooks() {
        return workbooks;
    }

    public TaskGenerator generator() {
        return generator;
    }

    public ExcelTaskEngine engine() {
        return engine;
    }

    public ExecutionLog log() {
        return log;
    }

    public ExecutorService workers() {
        return workers;
    }

    public Path outputDirectory() {
        return config.getOutputDirectory();
    }

    public Path reportsDirectory() {
        return config.getReportsDirectory();
    }

    /**
     * Lists saved session reports, newest first. A missing or unreadable directory
     * yields an empty list rather than an error, so the history screen stays usable.
     */
    public java.util.List<Path> reports() {
        try {
            return reports.list();
        } catch (IOException e) {
            log.add("Unable to list reports: " + e.getMessage());
            return java.util.List.of();
        }
    }

    public String readReport(Path report) throws IOException {
        return java.nio.file.Files.readString(report);
    }

    public SessionManager session() {
        return session;
    }

    public SessionManager requireSession() {
        SessionManager current = session;
        if (current == null) {
            throw new IllegalStateException("No session has been started");
        }
        return current;
    }

    public synchronized SessionManager startSession(SessionConfig sessionConfig) {
        SessionManager current = session;
        if (current != null) {
            SessionState state = current.state();
            if (state == SessionState.RUNNING || state == SessionState.PAUSED) {
                throw new IllegalStateException("A session is already active");
            }
        }
        SessionManager started = new SessionManager(sessionConfig);
        started.start();
        session = started;
        log.add("Session started (" + sessionConfig.display() + ")");
        return started;
    }

    public synchronized Path finishSession(boolean complete) throws IOException {
        SessionManager current = requireSession();
        if (complete) {
            current.complete();
        } else {
            current.stop();
        }
        Path report = reports.writeSessionReport(current.config(), current.stats(),
                current.results(), Instant.now());
        log.add("Session " + current.state() + ". Report saved to " + report);
        return report;
    }

    public String currentTask() {
        return currentTask;
    }

    public void setCurrentTask(String name) {
        currentTask = name == null || name.isBlank() ? "None" : name;
    }

    public Path currentWorkbook() {
        return currentWorkbook;
    }

    public void setCurrentWorkbook(Path workbook) {
        this.currentWorkbook = workbook;
    }

    public void record(TaskResult result) {
        SessionManager current = session;
        if (current == null) {
            log.add("Task “" + result.taskName() + "” finished as " + result.status()
                    + "; no session was running, so it was not recorded.");
            return;
        }
        boolean recorded = current.record(result);
        log.add("Task “" + result.taskName() + "” " + result.status()
                + (recorded ? " (recorded)" : " (session is not running; not recorded)"));
    }

    public void shutdown() {
        if (!shuttingDown.compareAndSet(false, true)) {
            return;
        }
        SessionManager current = session;
        if (current != null) {
            SessionState state = current.state();
            if (state == SessionState.RUNNING || state == SessionState.PAUSED) {
                current.stop();
            }
        }
        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, TimeUnit.SECONDS)) {
                workers.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            workers.shutdownNow();
        }
    }

    /**
     * Visible for tests: drops the singleton so an isolated instance can be built.
     */
    static synchronized void resetForTests() {
        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }
}
