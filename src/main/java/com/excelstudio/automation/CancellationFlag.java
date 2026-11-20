package com.excelstudio.automation;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe cancellation flag used to stop automation between actions.
 */
public final class CancellationFlag implements MutableCancellationToken {
    private final AtomicBoolean cancelled = new AtomicBoolean(false);

    @Override
    public boolean isCancelled() {
        return cancelled.get();
    }

    @Override
    public void cancel() {
        cancelled.set(true);
    }

    @Override
    public void reset() {
        cancelled.set(false);
    }
}
