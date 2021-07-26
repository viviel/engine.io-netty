package com.github.viviel.engine.io;

import com.github.viviel.engine.io.handler.CloseHandler;
import com.github.viviel.engine.io.handler.HandlerRegister;
import com.github.viviel.engine.io.handler.MessageHandler;
import com.github.viviel.engine.io.netty.EngineIONettyServer;

public class EngineIO {

    private final EngineIOContext ioCtx;

    private final EngineIONettyServer server;

    public EngineIO() {
        this.ioCtx = new EngineIOContext();
        this.server = new EngineIONettyServer(ioCtx);
    }

    public void start() {
        server.start();
    }

    public void messageHandler(MessageHandler h) {
        HandlerRegister register = ioCtx.handlerRegister();
        register.setMessageHandler(h);
    }

    public void closeHandler(CloseHandler c) {
        HandlerRegister register = ioCtx.handlerRegister();
        register.setCloseHandler(c);
    }
}
