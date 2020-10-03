package com.boruminc.borumjot.android.server;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRunner {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());

    /**
     * Interface for methods that are called only when the task is complete
     * @param <R> The type of the result
     */
    public interface Callback<R> {
        void onComplete(R result);
    }

    public <R> void executeAsync(Callable<R> callable, Callback<R> callback) {
        executor.execute(() -> { // Executes a Runnable that runs in a background thread
            try {
                final R result = callable.call();
                handler.post(() -> {
                    callback.onComplete(result);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
