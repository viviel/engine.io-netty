package com.github.viviel.engine.io.handler;

import com.github.viviel.engine.io.EngineIOClient;
import com.github.viviel.engine.io.protocol.Packet;

public interface MessageHandler {

    void message(EngineIOClient client, Packet msg);
}
