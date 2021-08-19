package com.github.viviel.engine.io;

import io.socket.engineio.client.Socket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

public class EngineIOClientTest {

    private Socket socket;

    @BeforeEach
    void setUp() throws URISyntaxException {
        Socket.Options options = getOptions();
        socket = new Socket("http://localhost:8888/", options);
    }

    private Socket.Options getOptions() {
        Socket.Options op = new Socket.Options();
        op.transports = new String[]{"polling"};
//        op.transports = new String[]{"websocket"};
        return op;
    }

    @Test
    void test() {
        socket.on(Socket.EVENT_OPEN, args -> {
            System.out.println("open");
            socket.send("hi");
        });
        socket.open();
        while (!Thread.interrupted()) {
            Utils.sleep(1000);
        }
    }

    private void send() {
        socket.send("test-message");
    }
}
