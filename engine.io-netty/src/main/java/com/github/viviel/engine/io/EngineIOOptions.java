package com.github.viviel.engine.io;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EngineIOOptions {

    private int pingInterval;
    private int pingTimeout;

    private ExecutorService es;

    public EngineIOOptions() {
        init();
    }

    private void init() {
        this.pingInterval = 10_000;
        this.pingTimeout = 5_000;
        this.es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    }

    public int getPingInterval() {
        return pingInterval;
    }

    public void setPingInterval(int pingInterval) {
        this.pingInterval = pingInterval;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }

    public ExecutorService getEs() {
        return es;
    }

    public void setEs(ExecutorService es) {
        this.es = es;
    }
}
