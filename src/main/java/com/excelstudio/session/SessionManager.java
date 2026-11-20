package com.excelstudio.session;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.excelstudio.model.TaskResult;

/**
 * Controls a practice session lifecycle and keeps an accurate elapsed time.
 *
 * <p>Time is measured with a monotonic clock, so pausing does not lose or gain
 * milliseconds. A session can be started only once; duplicate execution is rejected.
 */
public final class SessionManager {
    /**
     * Monotonic clock used for elapsed time.
     */
    @FunctionalInterface
    public interface Ticker {
        long nanoTime();
    }

    private final SessionConfig config;
    private final Ticker ticker;
    private final List<TaskResult> results = new CopyOnWriteArrayList<>();
    private final List<Consumer<SessionState>> listeners = new CopyOnWriteArrayList<>();

    private SessionState state = SessionState.IDLE;
    private long accumulatedNanos;
    private long segmentStartNanos;
    private boolean everStarted;

    public SessionManager(SessionConfig config) {
        this(config, System::nanoTime);
    }

    public SessionManager(SessionConfig config, Ticker ticker) {
        this.config = Objects.requireNonNull(config, "config");
        this.ticker = Objects.requireNonNull(ticker, "ticker");
    }

    public synchronized void start() {
        if (everStarted) {
            throw new IllegalStateException("Session has already been started");
        }
        everStarted = true;
        accumulatedNanos = 0;
        segmentStartNanos = ticker.nanoTime();
        transition(SessionState.RUNNING);
    }

    public synchronized void pause() {
        refreshDuration();
        requireActive();
        if (state == SessionState.PAUSED) {
            return;
        }
        accumulatedNanos = activeElapsedNanos();
        transition(SessionState.PAUSED);
    }

    public synchronized void resume() {
        refreshDuration();
        requireState(SessionState.PAUSED, "resume");
        segmentStartNanos = ticker.nanoTime();
        transition(SessionState.RUNNING);
    }

    public synchronized void stop() {
        refreshDuration();
        requireActive();
        accumulatedNanos = activeElapsedNanos();
        transition(SessionState.STOPPED);
    }

    public synchronized void complete() {
        refreshDuration();
        if (state == SessionState.COMPLETED) {
            return;
        }
        requireActive();
        accumulatedNanos = activeElapsedNanos();
        transition(SessionState.COMPLETED);
    }

    public synchronized void fail(String reason) {
        refreshDuration();
        if (state == SessionState.FAILED) {
            return;
        }
        requireActive();
        accumulatedNanos = activeElapsedNanos();
        transition(SessionState.FAILED);
    }

    public synchronized boolean record(TaskResult result) {
        Objects.requireNonNull(result, "result");
        refreshDuration();
        if (state != SessionState.RUNNING) {
            return false;
        }
        results.add(result);
        return true;
    }

    public synchronized SessionState state() {
        refreshDuration();
        return state;
    }

    public synchronized boolean isRunning() {
        refreshDuration();
        return state == SessionState.RUNNING;
    }

    public SessionConfig config() {
        return config;
    }

    public List<TaskResult> results() {
        return List.copyOf(results);
    }

    public synchronized Duration elapsed() {
        refreshDuration();
        return Duration.ofNanos(activeElapsedNanos());
    }

    public synchronized Duration remaining() {
        refreshDuration();
        Duration remaining = config.duration().minus(elapsed());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public synchronized SessionStats stats() {
        refreshDuration();
        return SessionStats.from(state, elapsed(), remaining(), results);
    }

    public void addStateListener(Consumer<SessionState> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
    }

    private long activeElapsedNanos() {
        if (state == SessionState.RUNNING) {
            return accumulatedNanos + Math.max(0, ticker.nanoTime() - segmentStartNanos);
        }
        return accumulatedNanos;
    }

    private void refreshDuration() {
        if (state != SessionState.RUNNING) {
            return;
        }
        long elapsedNanos = activeElapsedNanos();
        long durationNanos = config.duration().toNanos();
        if (elapsedNanos >= durationNanos) {
            accumulatedNanos = durationNanos;
            transition(SessionState.COMPLETED);
        }
    }

    private void requireActive() {
        if (state != SessionState.RUNNING && state != SessionState.PAUSED) {
            throw new IllegalStateException("Session is not active (state " + state + ")");
        }
    }

    private void requireState(SessionState expected, String action) {
        if (state != expected) {
            throw new IllegalStateException("Cannot " + action + " a session in state " + state);
        }
    }

    private void transition(SessionState next) {
        state = next;
        for (Consumer<SessionState> listener : listeners) {
            try {
                listener.accept(next);
            } catch (RuntimeException ignored) {
                // A misbehaving listener must not break session control.
            }
        }
    }
}
