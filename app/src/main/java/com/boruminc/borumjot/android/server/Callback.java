package com.boruminc.borumjot.android.server;

/**
 * Interface for methods that are called only when the task is complete
 * @param <R> The type of the result
 */
public interface Callback<R> {
    void onComplete(R result);
}
