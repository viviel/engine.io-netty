package com.github.viviel.engine.io.netty;

import com.github.viviel.engine.io.EngineIOClientImpl;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.util.AttributeKey;

/**
 * netty attribute
 */
public class NettyAttr {

    public static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER = AttributeKey.valueOf("handshaker");

    public static final AttributeKey<String> ORIGIN = AttributeKey.valueOf("origin");

    public static final AttributeKey<EngineIOClientImpl> CLIENT = AttributeKey.valueOf("client");
}
