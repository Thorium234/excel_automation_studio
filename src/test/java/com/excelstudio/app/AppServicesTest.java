package com.excelstudio.app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import com.excelstudio.config.AppConfig;
import com.excelstudio.model.TaskResult;
import com.excelstudio.model.TaskStatus;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.session.SessionConfig;
import com.excelstudio.session.SessionState;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppServicesTest {

    @TempDir
    Path temp;

    private AppServices services;

    @BeforeEach
    void setUp() {
        AppServices.resetForTests();
        AppConfig config = AppConfig.load(temp.resolve("config.properties"));
        config.setOutputDirectory(temp.resolve("workbooks"));
        services = AppServices.initialize(config);
    }

    @AfterEach
    void tearDown() {
        AppServices.resetForTests();
    }

    @Test
    void startsASessionOnlyOnce() {
        services.startSession(SessionConfig.ofMinutes(30));

        assertNotNull(services.session());
        assertEquals(SessionState.RUNNING, services.session().state());
        assertThrows(IllegalStateException.class,
                () -> services.startSession(SessionConfig.ofMinutes(30)));
    }

    @Test
    void recordsTaskResultsOnlyWhileSessionRuns() {
        TaskResult result = result("Data entry");

        services.record(result);
        assertTrue(services.session() == null);

        services.startSession(SessionConfig.ofMinutes(30));
        services.record(result);
        services.record(result("Sorting"));

        assertEquals(2, services.session().results().size());
        assertEquals(2, services.session().stats().completed());
    }

    @Test
    void finishingASessionWritesAReportThatHistoryCanList() throws Exception {
        services.startSession(SessionConfig.ofMinutes(30));
        services.record(result("Enter records"));

        Path report = services.finishSession(true);

        assertEquals(SessionState.COMPLETED, services.session().state());
        assertTrue(Files.isRegularFile(report));
        List<Path> reports = services.reports();
        assertTrue(reports.contains(report));
        assertTrue(services.readReport(report).contains("Enter records"));
    }

    @Test
    void stoppingASessionAlsoWritesAReport() throws Exception {
        services.startSession(SessionConfig.ofMinutes(30));
        services.record(result("Charts"));

        Path report = services.finishSession(false);

        assertEquals(SessionState.STOPPED, services.session().state());
        assertTrue(Files.isRegularFile(report));
    }

    @Test
    void finishingWithoutASessionIsRejected() {
        assertThrows(IllegalStateException.class, () -> services.finishSession(false));
    }

    @Test
    void shutdownStopsARunningSession() {
        services.startSession(SessionConfig.ofMinutes(30));

        services.shutdown();

        assertFalse(services.session().isRunning());
        assertEquals(SessionState.STOPPED, services.session().state());
        assertTrue(services.reports().size() >= 1);
        assertTrue(services.reports().get(0).getFileName().toString().endsWith(".md"));
    }

    @Test
    void shutdownContinuesAndLogsWhenStoppedSessionReportCannotBeWritten() throws Exception {
        Files.writeString(temp.resolve("reports"), "blocking file");
        services.startSession(SessionConfig.ofMinutes(30));

        services.shutdown();

        assertEquals(SessionState.STOPPED, services.session().state());
        assertTrue(services.log().entries().stream()
                .anyMatch(entry -> entry.contains("Unable to save stopped-session report")));
    }

    @Test
    void logsEveryRecordedTask() {
        services.startSession(SessionConfig.ofMinutes(30));
        services.record(result("Formatting"));

        assertTrue(services.log().entries().stream()
                .anyMatch(entry -> entry.contains("Formatting") && entry.contains("PASSED")));
    }

    private static TaskResult result(String name) {
        Instant now = Instant.parse("2026-05-01T09:00:00Z");
        return TaskResult.of("id", name, TaskStatus.PASSED, now, now.plusSeconds(5), null,
                ValidationResult.passed(2), null);
    }
}
