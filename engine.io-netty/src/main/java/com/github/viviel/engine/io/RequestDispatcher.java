package com.github.viviel.engine.io;

import com.github.viviel.engine.io.netty.NettyAttr;
import com.github.viviel.engine.io.protocol.Packet;
import com.github.viviel.engine.io.protocol.PacketType;
import com.github.viviel.engine.io.protocol.PollingProcessor;
import com.github.viviel.engine.io.protocol.WebsocketProcessor;
import com.github.viviel.engine.io.utils.ServerYeast;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static io.netty.handler.codec.http.HttpHeaderNames.ORIGIN;

public class RequestDispatcher {

    private final EngineIOContext ioCtx;

    private final EngineIOOptions opt;

    private final PollingProcessor pollingProcessor;

    private final WebsocketProcessor websocketProcessor;

    private final EngineIOClientContainer clientContainer;

    public RequestDispatcher(EngineIOContext ioCtx) {
        this.ioCtx = ioCtx;
        this.opt = ioCtx.ioOpt();
        this.pollingProcessor = ioCtx.pollingProcessor();
        this.websocketProcessor = ioCtx.websocketProcessor();
        this.clientContainer = ioCtx.clientContainer();
    }

    public void dispatch(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            readFullHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            readWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void readFullHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        QueryStringDecoder qsd = new QueryStringDecoder(req.uri());
        List<String> sids = qsd.parameters().get("sid");
        setChannelAttr(ctx, req);
        if (sids == null || sids.isEmpty()) {
            readFullHttpRequestNewClient(ctx, req);
        } else {
            String sid = sids.get(0);
            readFullHttpRequestOldClient(ctx, req, sid);
        }
    }

    private void setChannelAttr(ChannelHandlerContext ctx, FullHttpRequest req) {
        String origin = req.headers().get(ORIGIN);
        ctx.channel().attr(NettyAttr.ORIGIN).set(origin);
    }

    private void readFullHttpRequestNewClient(ChannelHandlerContext ctx, FullHttpRequest req) {
        EngineIOClientImpl c = createClient();
        Packet packet = getInitChannelPacket(c);
        if (needUpgradeWS(req)) {
            c.setCurrentTransport(TransportType.WEBSOCKET);
            c.setWebsocketCtx(ctx);
            upgradeWS(ctx, req);
            ctx.channel().attr(NettyAttr.CLIENT).set(c);
            websocketProcessor.send(ctx, packet).syncUninterruptibly();
        } else {
            c.setCurrentTransport(TransportType.POLLING);
            pollingProcessor.send(ctx, packet).syncUninterruptibly();
        }
        ioCtx.clientContainer().add(c);
        ioCtx.handlerRegister().getOpenHandler().open(c);
        c.setStatus(EngineIOClientImpl.OPEN);
        c.ping();
    }

    private EngineIOClientImpl createClient() {
        String sid = ServerYeast.yeast();
        return new EngineIOClientImpl(sid, ioCtx);
    }

    private Packet getInitChannelPacket(EngineIOClientImpl c) {
        JSONObject resp = new JSONObject();
        resp.put("sid", c.getSid());
        resp.put("upgrades", Collections.singletonList(TransportType.WEBSOCKET.type()));
        resp.put("pingInterval", opt.getPingInterval());
        resp.put("pingTimeout", opt.getPingTimeout());
        Packet packet = new Packet(PacketType.OPEN);
        packet.setData(resp.toString());
        return packet;
    }

    private boolean needUpgradeWS(FullHttpRequest req) {
        HttpHeaders headers = req.headers();
        String upgrade = headers.get("Upgrade");
        return Objects.equals(upgrade, "websocket");
    }

    private void upgradeWS(ChannelHandlerContext ctx, FullHttpRequest req) {
        final Channel channel = ctx.channel();
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(req), null, true
        );
        WebSocketServerHandshaker handShaker = factory.newHandshaker(req);
        if (handShaker != null) {
            handShaker.handshake(channel, req).syncUninterruptibly();
            channel.attr(NettyAttr.HANDSHAKER).set(handShaker);
        } else {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        }
    }

    private String getWebSocketLocation(HttpRequest req) {
        String protocol = "ws://";
        return protocol + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    }

    private void readFullHttpRequestOldClient(ChannelHandlerContext ctx, FullHttpRequest req, String sid) {
        EngineIOClientImpl c = clientContainer.get(sid);
        if (c == null) {
            pollingProcessor.send(ctx, new Packet(PacketType.ERROR))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            if (needUpgradeWS(req)) {
                upgradeWS(ctx, req);
                c.setWebsocketCtx(ctx);
                ctx.channel().attr(NettyAttr.CLIENT).set(c);
            } else {
                handleHttpRequest(ctx, c, req);
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, EngineIOClientImpl c, FullHttpRequest req) {
        TransportInfo<FullHttpRequest> info = TransportInfo.<FullHttpRequest>builder()
                .ctx(ctx).client(c).type(TransportType.POLLING).data(req)
                .build();
        pollingProcessor.process(info);
    }

    private void readWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (!ctx.channel().hasAttr(NettyAttr.CLIENT)) {
            websocketProcessor.send(ctx, new Packet(PacketType.ERROR))
                    .addListener(ChannelFutureListener.CLOSE);
        } else {
            doReadWebSocketFrame(ctx, frame);
        }
    }

    private void doReadWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        Channel channel = ctx.channel();
        if (frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = channel.attr(NettyAttr.HANDSHAKER).get();
            handshaker.close(channel, (CloseWebSocketFrame) frame.retain());
        } else if (frame instanceof PingWebSocketFrame) {
            channel.write(new PongWebSocketFrame(frame.content().retain()));
        } else if (frame instanceof TextWebSocketFrame) {
            EngineIOClientImpl c = ctx.channel().attr(NettyAttr.CLIENT).get();
            TransportInfo<TextWebSocketFrame> info = TransportInfo.<TextWebSocketFrame>builder()
                    .ctx(ctx).client(c).type(TransportType.WEBSOCKET).data((TextWebSocketFrame) frame)
                    .build();
            websocketProcessor.process(info);
        }
    }
}
