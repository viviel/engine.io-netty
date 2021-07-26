package com.github.viviel.engine.io;

import com.github.viviel.engine.io.handler.HandlerRegister;
import com.github.viviel.engine.io.protocol.PollingProcessor;
import com.github.viviel.engine.io.protocol.WebsocketProcessor;
import com.github.viviel.engine.io.timer.HashedWheelTimeoutTimer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EngineIOContext {

    private final ExecutorService es;

    private final EngineIOOptions ioOpt;

    private final HashedWheelTimeoutTimer timer;

    private final HandlerRegister handlerRegister;

    private final EngineIOClientContainer clientContainer;

    private final PollingProcessor pollingProcessor;

    private final WebsocketProcessor websocketProcessor;

    private final RequestDispatcher requestDispatcher;

    /**
     * The construction sequence cannot be changed at will.
     */
    public EngineIOContext() {
        this.es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.ioOpt = new EngineIOOptions();
        this.timer = new HashedWheelTimeoutTimer();
        this.handlerRegister = new HandlerRegister();
        this.clientContainer = new EngineIOClientContainer();
        this.pollingProcessor = new PollingProcessor(this);
        this.websocketProcessor = new WebsocketProcessor(this);
        this.requestDispatcher = new RequestDispatcher(this);
    }

    public ExecutorService executor() {
        return es;
    }

    public EngineIOOptions ioOpt() {
        return ioOpt;
    }

    public HashedWheelTimeoutTimer timer() {
        return timer;
    }

    public HandlerRegister handlerRegister() {
        return handlerRegister;
    }

    public EngineIOClientContainer clientContainer() {
        return clientContainer;
    }

    public PollingProcessor pollingProcessor() {
        return pollingProcessor;
    }

    public WebsocketProcessor websocketProcessor() {
        return websocketProcessor;
    }

    public RequestDispatcher requestDispatcher() {
        return requestDispatcher;
    }
}
