package com.github.viviel.engine.io;

public enum TransportType {

    POLLING("polling"),
    WEBSOCKET("websocket"),
    ;

    private final String type;

    TransportType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    public boolean is(EngineIOClientImpl c) {
        return this.equals(c.getCurrentTransport());
    }
}
