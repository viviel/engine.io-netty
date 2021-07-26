package com.github.viviel.engine.io.timer;

import com.github.viviel.engine.io.EngineIOClientImpl;

import java.util.Objects;
import java.util.concurrent.Executor;

public class TimerKey {

    public enum Type {PING, PING_TIMEOUT, ACK_TIMEOUT, UPGRADE_TIMEOUT}

    private final Type type;
    private final String sid;
    private final Executor e;

    private TimerKey(Type type, String sid, Executor e) {
        if (type == null || sid == null) {
            throw new IllegalArgumentException();
        }
        this.type = type;
        this.sid = sid;
        this.e = e;
    }

    public static TimerKey of(Type type, EngineIOClientImpl c) {
        String sid = c.getSid();
        Executor executor = c.getExecutor();
        return new TimerKey(type, sid, executor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimerKey timerKey = (TimerKey) o;
        return type == timerKey.type && sid.equals(timerKey.sid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, sid);
    }

    public Executor executor() {
        return e;
    }
}
