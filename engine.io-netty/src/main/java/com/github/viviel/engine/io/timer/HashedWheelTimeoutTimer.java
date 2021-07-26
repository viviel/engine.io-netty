package com.github.viviel.engine.io.timer;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.internal.PlatformDependent;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class HashedWheelTimeoutTimer implements Timer {

    private final ConcurrentMap<TimerKey, Timeout> timeouts;
    private final HashedWheelTimer timer;

    public HashedWheelTimeoutTimer() {
        this.timer = new HashedWheelTimer();
        this.timeouts = PlatformDependent.newConcurrentHashMap();
    }

    @Override
    public void cancel(TimerKey key) {
        Timeout timeout = timeouts.remove(key);
        if (timeout != null) {
            timeout.cancel();
        }
    }

    @Override
    public void schedule(TimerKey key, long delay, Runnable runnable) {
        cancel(key);
        Timeout timeout = timer.newTimeout(t -> key.executor().execute(() -> {
            try {
                runnable.run();
            } finally {
                timeouts.remove(key);
            }
        }), delay, TimeUnit.MILLISECONDS);
        replaceTimeout(key, timeout);
    }

    @Override
    public void shutdown() {
        timer.stop();
    }

    private void replaceTimeout(TimerKey key, Timeout target) {
        Timeout oldTimeout;
        if (target.isExpired()) {
            // no need to put already expired timeout to timeouts map.
            // simply remove old timeout
            oldTimeout = timeouts.remove(key);
        } else {
            oldTimeout = timeouts.put(key, target);
        }
        // if there was old timeout, cancel it
        if (oldTimeout != null) {
            oldTimeout.cancel();
        }
    }
}
