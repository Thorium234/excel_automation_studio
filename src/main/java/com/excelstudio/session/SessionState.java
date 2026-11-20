package com.excelstudio.session;

/**
 * Lifecycle of a practice session.
 */
public enum SessionState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED,
    STOPPED,
    FAILED
}
