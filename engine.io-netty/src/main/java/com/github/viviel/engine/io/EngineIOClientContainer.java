package com.github.viviel.engine.io;

import java.util.HashMap;
import java.util.Map;

public class EngineIOClientContainer {

    private final Map<String, EngineIOClientImpl> clients;

    public EngineIOClientContainer() {
        this.clients = new HashMap<>();
    }

    public void add(EngineIOClientImpl c) {
        String sid = c.getSid();
        clients.put(sid, c);
    }

    public void remove(String sid) {
        clients.remove(sid);
    }

    public EngineIOClientImpl get(String sid) {
        return clients.get(sid);
    }
}
