package com.github.viviel.engine.io.protocol;

import com.github.viviel.engine.io.EngineIOClientImpl;
import com.github.viviel.engine.io.EngineIOContext;
import com.github.viviel.engine.io.TransportInfo;
import com.github.viviel.engine.io.TransportType;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.nio.charset.StandardCharsets;

public class WebsocketProcessor extends AbstractProcessor {

    public WebsocketProcessor(EngineIOContext ioCtx) {
        super(ioCtx);
    }

    public void process(TransportInfo<TextWebSocketFrame> info) {
        TextWebSocketFrame frame = info.getData();
        EngineIOClientImpl c = info.getClient();
        String msg = frame.text();
        Packet packet = Parser.decodePacket(msg);
        switch (packet.getType()) {
            case PING: {
                packet.setType(PacketType.PONG);
                send(c.getWebsocketCtx(), packet).syncUninterruptibly();
                c.flushPolling();
                c.send(Packet.NOOP);
                break;
            }
            case UPGRADE: {
                c.setCurrentTransport(TransportType.WEBSOCKET);
                c.flushWebsocket();
                break;
            }
            default: {
                processPacket(c, packet);
                break;
            }
        }
    }

    @Override
    protected ChannelFuture send(ChannelHandlerContext ctx, String s) {
        ByteBuf bb = ctx.alloc().buffer();
        bb.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        WebSocketFrame frame = new TextWebSocketFrame(bb);
        return ctx.writeAndFlush(frame);
    }
}
