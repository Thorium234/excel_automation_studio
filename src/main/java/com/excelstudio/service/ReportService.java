package com.excelstudio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.excelstudio.model.TaskResult;
import com.excelstudio.session.SessionConfig;
import com.excelstudio.session.SessionStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Writes a human-readable Markdown report for a finished session.
 */
public final class ReportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportService.class);
    private static final DateTimeFormatter FILE_STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneId.systemDefault());

    private final Path reportsDirectory;

    public ReportService(Path reportsDirectory) {
        this.reportsDirectory = reportsDirectory;
    }

    public Path writeSessionReport(SessionConfig config, SessionStats stats,
                                   List<TaskResult> results, Instant generatedAt) throws IOException {
        Files.createDirectories(reportsDirectory);
        Path target = uniqueFile(generatedAt);

        StringBuilder report = new StringBuilder();
        report.append("# Excel Automation Studio session report\n\n");
        report.append("- Generated: ").append(generatedAt).append('\n');
        report.append("- State: ").append(stats.state()).append('\n');
        report.append("- Planned duration: ").append(config.display()).append('\n');
        report.append("- Elapsed: ").append(stats.elapsedText()).append('\n');
        report.append("- Remaining: ").append(stats.remainingText()).append('\n');
        report.append("- Completed tasks: ").append(stats.completed()).append('\n');
        report.append("- Failed tasks: ").append(stats.failed()).append('\n');
        report.append('\n');
        report.append("| Task | Status | Duration | Workbook | Validation |\n");
        report.append("|---|---|---|---|---|\n");

        for (TaskResult result : results) {
            report.append("| ").append(escape(result.taskName()))
                    .append(" | ").append(result.status())
                    .append(" | ").append(formatDuration(result.duration()))
                    .append(" | ").append(escape(workbook(result)))
                    .append(" | ").append(escape(validation(result)))
                    .append(" |\n");
        }

        Files.writeString(target, report.toString());
        LOGGER.info("Session report written to {}", target);
        return target;
    }

    private Path uniqueFile(Instant generatedAt) {
        String base = "session-" + FILE_STAMP.format(generatedAt);
        Path candidate = reportsDirectory.resolve(base + ".md");
        int counter = 2;
        while (Files.exists(candidate)) {
            candidate = reportsDirectory.resolve(base + "-" + counter + ".md");
            counter++;
        }
        return candidate;
    }

    private static String workbook(TaskResult result) {
        return result.workbook() == null ? "" : result.workbook().toString();
    }

    private static String validation(TaskResult result) {
        if (result.error() != null && !result.error().isBlank()) {
            return result.error();
        }
        return result.validation() == null ? "" : result.validation().summary();
    }

    private static String formatDuration(Duration duration) {
        return String.format(Locale.ROOT, "%.1f s", duration.toMillis() / 1000.0);
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("|", "\\|").replace("\n", " ");
    }

    public Path directory() {
        return reportsDirectory;
    }

    /**
     * Lists saved Markdown reports, newest first.
     */
    public List<Path> list() throws IOException {
        if (!Files.isDirectory(reportsDirectory)) {
            return List.of();
        }
        try (var stream = Files.list(reportsDirectory)) {
            return stream
                    .filter(path -> path.getFileName().toString().endsWith(".md"))
                    .sorted(java.util.Comparator.comparing(
                            (Path path) -> path.getFileName().toString()).reversed())
                    .toList();
        }
    }
}
