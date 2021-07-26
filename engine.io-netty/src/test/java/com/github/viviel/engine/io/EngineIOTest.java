package com.github.viviel.engine.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EngineIOTest extends AbstractTest {

    private EngineIO engineIO;

    @BeforeEach
    void setUp() {
        engineIO = new EngineIO();
    }

    @Test
    void test1() {
        init();
        engineIO.start();
        Utils.block();
    }

    private void init() {
        engineIO.messageHandler((client, msg) -> {
//            System.out.println(msg);
            client.send("haha");
        });
        engineIO.closeHandler(client -> {
            System.out.println("close: " + client);
        });
    }
}
