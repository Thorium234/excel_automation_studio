package com.excelstudio.service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory execution log shown in the dashboard. Messages are timestamped so the
 * UI can display a running history without re-reading log files.
 */
public final class ExecutionLog {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int MAX_ENTRIES = 500;

    private final List<String> entries = new CopyOnWriteArrayList<>();

    public void add(String message) {
        entries.add(TIME.format(LocalTime.now()) + "  " + message);
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
    }

    public List<String> entries() {
        return List.copyOf(entries);
    }
}
