package com.excelstudio.automation;

/**
 * Marks a cancellation token whose state can be changed.
 */
public interface MutableCancellationToken extends CancellationToken {
    void cancel();

    void reset();
}
