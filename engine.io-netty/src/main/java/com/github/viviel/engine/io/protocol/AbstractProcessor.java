package com.github.viviel.engine.io.protocol;

import com.github.viviel.engine.io.EngineIOClientImpl;
import com.github.viviel.engine.io.EngineIOContext;
import com.github.viviel.engine.io.handler.HandlerRegister;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

public abstract class AbstractProcessor {

    private final HandlerRegister handlerRegister;

    public AbstractProcessor(EngineIOContext ioCtx) {
        this.handlerRegister = ioCtx.handlerRegister();
    }

    protected boolean processPacket(EngineIOClientImpl c, Packet packet) {
        switch (packet.getType()) {
            case CLOSE: {
                c.close();
                return false;
            }
            case PONG: {
                c.cancelPingTimeout();
                c.ping();
                return true;
            }
            case MESSAGE: {
                handlerRegister.getMessageHandler().message(c, packet);
                return true;
            }
            default: {
                return false;
            }
        }
    }

    public ChannelFuture send(ChannelHandlerContext ctx, Packet packet) {
        return Parser.encodePacket(packet, msg -> send(ctx, msg));
    }

    protected abstract ChannelFuture send(ChannelHandlerContext ctx, String s);
}
