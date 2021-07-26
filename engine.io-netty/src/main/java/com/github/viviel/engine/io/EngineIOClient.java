package com.github.viviel.engine.io;

import com.github.viviel.engine.io.protocol.Packet;

public interface EngineIOClient {

    void send(Packet p);

    void send(String msg);

    void close();
}
