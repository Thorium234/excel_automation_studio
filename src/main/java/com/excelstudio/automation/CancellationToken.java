package com.excelstudio.automation;

/**
 * Read-only cancellation signal shared by automation components.
 */
@FunctionalInterface
public interface CancellationToken {
    boolean isCancelled();

    static CancellationToken none() {
        return () -> false;
    }
}
