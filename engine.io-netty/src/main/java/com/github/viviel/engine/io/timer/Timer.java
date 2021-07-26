package com.github.viviel.engine.io.timer;

public interface Timer {

    void cancel(TimerKey key);

    /**
     * @param delay milliseconds
     */
    void schedule(TimerKey key, long delay, Runnable runnable);

    void shutdown();
}
