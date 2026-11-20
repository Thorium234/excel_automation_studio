package com.excelstudio.session;

import java.time.Duration;

import com.excelstudio.model.TaskResult;

/**
 * Snapshot of a session's progress.
 */
public record SessionStats(SessionState state, Duration elapsed, Duration remaining,
                           int completed, int failed, int totalTasks) {

    public static SessionStats empty(SessionState state, Duration remaining) {
        return new SessionStats(state, Duration.ZERO, remaining, 0, 0, 0);
    }

    public static SessionStats from(SessionState state, Duration elapsed, Duration remaining,
                                    java.util.List<TaskResult> results) {
        int completed = 0;
        int failed = 0;
        for (TaskResult result : results) {
            if (result.succeeded()) {
                completed++;
            } else {
                failed++;
            }
        }
        return new SessionStats(state, elapsed, remaining, completed, failed, results.size());
    }

    public double progress(Duration planned) {
        if (planned == null || planned.isZero()) {
            return 0;
        }
        double ratio = (double) elapsed.toMillis() / planned.toMillis();
        return Math.max(0, Math.min(1, ratio));
    }

    public String elapsedText() {
        long totalSeconds = elapsed.toSeconds();
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60,
                totalSeconds % 60);
    }

    public String remainingText() {
        long totalSeconds = Math.max(0, remaining.toSeconds());
        return String.format("%02d:%02d:%02d", totalSeconds / 3600, (totalSeconds % 3600) / 60,
                totalSeconds % 60);
    }
}
