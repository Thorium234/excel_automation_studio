package com.excelstudio.session;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.excelstudio.model.TaskResult;
import com.excelstudio.model.TaskStatus;
import com.excelstudio.model.ValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionManagerTest {

    private static final class FakeTicker implements SessionManager.Ticker {
        private long nanos;

        @Override
        public long nanoTime() {
            return nanos;
        }

        void advance(Duration duration) {
            nanos += duration.toNanos();
        }
    }

    private final FakeTicker ticker = new FakeTicker();
    private final SessionManager session = new SessionManager(SessionConfig.ofMinutes(30), ticker);

    @Test
    void startsIdleAndTracksElapsedTime() {
        assertEquals(SessionState.IDLE, session.state());
        assertEquals(Duration.ZERO, session.elapsed());

        session.start();
        ticker.advance(Duration.ofSeconds(75));

        assertEquals(SessionState.RUNNING, session.state());
        assertEquals(Duration.ofSeconds(75), session.elapsed());
    }

    @Test
    void rejectsDuplicateStart() {
        session.start();

        assertThrows(IllegalStateException.class, session::start);
    }

    @Test
    void pauseFreezesAndResumeContinuesElapsedTime() {
        session.start();
        ticker.advance(Duration.ofSeconds(10));
        session.pause();

        ticker.advance(Duration.ofSeconds(60));
        assertEquals(Duration.ofSeconds(10), session.elapsed());
        assertEquals(SessionState.PAUSED, session.state());

        session.resume();
        ticker.advance(Duration.ofSeconds(5));

        assertEquals(Duration.ofSeconds(15), session.elapsed());
        assertEquals(SessionState.RUNNING, session.state());
    }

    @Test
    void remainingTimeNeverGoesNegative() {
        session.start();
        ticker.advance(Duration.ofMinutes(45));

        assertEquals(Duration.ZERO, session.remaining());
    }

    @Test
    void stopEndsTheSessionPermanently() {
        session.start();
        ticker.advance(Duration.ofMinutes(2));
        session.stop();

        assertEquals(SessionState.STOPPED, session.state());
        assertEquals(Duration.ofMinutes(2), session.elapsed());
        assertThrows(IllegalStateException.class, session::resume);
        assertThrows(IllegalStateException.class, session::start);
    }

    @Test
    void completeRecordsTheFinalDuration() {
        session.start();
        ticker.advance(Duration.ofSeconds(3));
        session.complete();

        assertEquals(SessionState.COMPLETED, session.state());
        assertEquals(Duration.ofSeconds(3), session.elapsed());
    }

    @Test
    void cannotStopAnIdleSession() {
        assertThrows(IllegalStateException.class, session::stop);
        assertThrows(IllegalStateException.class, session::pause);
        assertThrows(IllegalStateException.class, session::resume);
    }

    @Test
    void recordsTaskResultsOnlyWhileRunning() {
        TaskResult passed = result(TaskStatus.PASSED);
        TaskResult failed = result(TaskStatus.FAILED);

        assertFalse(session.record(passed));

        session.start();
        assertTrue(session.record(passed));
        assertTrue(session.record(failed));

        SessionStats stats = session.stats();
        assertEquals(1, stats.completed());
        assertEquals(1, stats.failed());
        assertEquals(2, stats.totalTasks());
    }

    @Test
    void notifiesStateListeners() {
        List<SessionState> states = new ArrayList<>();
        session.addStateListener(states::add);

        session.start();
        session.pause();
        session.resume();
        session.stop();

        assertEquals(List.of(SessionState.RUNNING, SessionState.PAUSED, SessionState.RUNNING,
                SessionState.STOPPED), states);
    }

    @Test
    void aThrowingListenerDoesNotBreakControl() {
        session.addStateListener(state -> {
            throw new IllegalStateException("listener failure");
        });

        session.start();

        assertEquals(SessionState.RUNNING, session.state());
    }

    @Test
    void progressIsBounded() {
        session.start();
        ticker.advance(Duration.ofMinutes(15));

        double progress = session.stats().progress(session.config().duration());

        assertEquals(0.5, progress, 0.001);
    }

    @Test
    void configuredDurationAutomaticallyCompletesAndStopsRecording() {
        session.start();
        ticker.advance(Duration.ofMinutes(30));

        assertEquals(SessionState.COMPLETED, session.state());
        assertEquals(Duration.ZERO, session.remaining());
        assertFalse(session.record(result(TaskStatus.PASSED)));
    }

    @Test
    void pausedTimeDoesNotConsumeConfiguredDuration() {
        session.start();
        ticker.advance(Duration.ofMinutes(10));
        session.pause();
        ticker.advance(Duration.ofMinutes(30));

        assertEquals(Duration.ofMinutes(10), session.elapsed());
        assertEquals(SessionState.PAUSED, session.state());
        assertEquals(Duration.ofMinutes(20), session.remaining());
    }

    @Test
    void failRequiresAnActiveSession() {
        assertThrows(IllegalStateException.class, () -> session.fail("failure"));
        session.start();
        session.fail("failure");
        assertEquals(SessionState.FAILED, session.state());
    }

    private static TaskResult result(TaskStatus status) {
        return TaskResult.of("id", "task", status, java.time.Instant.now(),
                java.time.Instant.now(), null, ValidationResult.passed(1), null);
    }
}
