package com.excelstudio.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.excelstudio.model.TaskResult;
import com.excelstudio.model.TaskStatus;
import com.excelstudio.model.ValidationResult;
import com.excelstudio.session.SessionConfig;
import com.excelstudio.session.SessionState;
import com.excelstudio.session.SessionStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    @TempDir
    Path reports;

    @Test
    void writesASummaryWithEveryTask() throws Exception {
        Instant started = Instant.parse("2026-05-01T09:00:00Z");
        List<TaskResult> results = List.of(
                TaskResult.of("1", "Enter employee records", TaskStatus.PASSED, started,
                        started.plusSeconds(30), Path.of("data/workbooks/a.xlsx"),
                        ValidationResult.passed(5), null),
                TaskResult.of("2", "Sort records by ID", TaskStatus.FAILED, started,
                        started.plusSeconds(45), Path.of("data/workbooks/b.xlsx"),
                        ValidationResult.failed(List.of(
                                ValidationResult.ValidationIssue.of("row 3 is out of order")), 4),
                        null));

        SessionStats stats = new SessionStats(SessionState.COMPLETED,
                Duration.ofMinutes(10), Duration.ofMinutes(20), 1, 1, 2);
        Path report = new ReportService(reports)
                .writeSessionReport(SessionConfig.ofMinutes(30), stats, results, started);

        String content = Files.readString(report);
        assertTrue(content.contains("# Excel Automation Studio session report"));
        assertTrue(content.contains("Enter employee records"));
        assertTrue(content.contains("Sort records by ID"));
        assertTrue(content.contains("PASSED"));
        assertTrue(content.contains("FAILED"));
        assertTrue(content.contains("row 3 is out of order"));
        assertTrue(content.contains("00:10:00"));
        assertTrue(Files.isRegularFile(report));
    }

    @Test
    void neverOverwritesAnExistingReport() throws Exception {
        Instant now = Instant.parse("2026-05-01T09:00:00Z");
        SessionStats stats = new SessionStats(SessionState.STOPPED, Duration.ZERO,
                Duration.ofMinutes(30), 0, 0, 0);
        ReportService service = new ReportService(reports);

        Path first = service.writeSessionReport(SessionConfig.ofMinutes(30), stats, List.of(), now);
        Path second = service.writeSessionReport(SessionConfig.ofMinutes(30), stats, List.of(), now);

        assertNotEquals(first, second);
        assertTrue(Files.isRegularFile(first));
        assertTrue(Files.isRegularFile(second));
    }

    @Test
    void escapesTableSeparatorsInTaskNames() throws Exception {
        Instant now = Instant.parse("2026-05-01T09:00:00Z");
        TaskResult result = TaskResult.of("1", "A | B", TaskStatus.PASSED, now, now, null,
                ValidationResult.passed(1), null);
        SessionStats stats = new SessionStats(SessionState.COMPLETED, Duration.ZERO,
                Duration.ofMinutes(30), 1, 0, 1);

        String content = Files.readString(new ReportService(reports)
                .writeSessionReport(SessionConfig.ofMinutes(30), stats, List.of(result), now));

        assertTrue(content.contains("A \\| B"));
    }

    @Test
    void listsReportsNewestFirstAndIgnoresOtherFiles() throws Exception {
        ReportService service = new ReportService(reports);
        SessionStats stats = new SessionStats(SessionState.STOPPED, Duration.ZERO,
                Duration.ofMinutes(30), 0, 0, 0);

        Path older = service.writeSessionReport(SessionConfig.ofMinutes(30), stats, List.of(),
                Instant.parse("2026-05-01T09:00:00Z"));
        Path newer = service.writeSessionReport(SessionConfig.ofMinutes(30), stats, List.of(),
                Instant.parse("2026-05-02T09:00:00Z"));
        Files.writeString(reports.resolve("notes.txt"), "not a report");

        assertEquals(List.of(newer, older), service.list());
    }

    @Test
    void listIsEmptyWhenTheDirectoryDoesNotExist() throws Exception {
        assertTrue(new ReportService(reports.resolve("missing")).list().isEmpty());
    }
}
