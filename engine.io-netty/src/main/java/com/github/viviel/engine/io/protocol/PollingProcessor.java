package com.github.viviel.engine.io.protocol;

import com.github.viviel.engine.io.EngineIOClientImpl;
import com.github.viviel.engine.io.EngineIOContext;
import com.github.viviel.engine.io.TransportInfo;
import com.github.viviel.engine.io.netty.NettyAttr;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class PollingProcessor extends AbstractProcessor {

    public PollingProcessor(EngineIOContext ioCtx) {
        super(ioCtx);
    }

    public void process(TransportInfo<FullHttpRequest> info) {
        ChannelHandlerContext ctx = info.getCtx();
        FullHttpRequest req = info.getData();
        if (HttpMethod.GET.equals(req.method())) {
            processGet(info);
        } else if (HttpMethod.POST.equals(req.method())) {
            processPost(info);
        } else {
            FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
            ctx.write(resp).addListener(ChannelFutureListener.CLOSE);
        }
    }

    private void processGet(TransportInfo<FullHttpRequest> info) {
        ChannelHandlerContext ctx = info.getCtx();
        EngineIOClientImpl c = info.getClient();
        c.setPollingCtx(ctx);
        c.poll();
        c.flushPolling();
    }

    private void processPost(TransportInfo<FullHttpRequest> info) {
        ChannelHandlerContext ctx = info.getCtx();
        EngineIOClientImpl c = info.getClient();
        FullHttpRequest req = info.getData();
        ByteBuf content = req.content();
        String msg = content.toString(CharsetUtil.UTF_8);
        Parser.decodePayload(msg, (p -> processPacket(c, p)));
        ChannelFuture cf = send(ctx, Packet.NOOP);
        if (!HttpUtil.isKeepAlive(req)) {
            cf.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public void send(ChannelHandlerContext ctx, List<Packet> packets) {
        Parser.encodePayload(packets, data -> send(ctx, data));
    }

    @Override
    protected ChannelFuture send(ChannelHandlerContext ctx, String s) {
        Channel channel = ctx.channel();
        ByteBuf bb = ctx.alloc().buffer();
        bb.writeBytes(s.getBytes(StandardCharsets.UTF_8));
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK, bb);
        setHeaders(channel, resp);
        return channel.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }

    private void setHeaders(Channel channel, FullHttpResponse resp) {
        HttpHeaders headers = resp.headers();
        headers.set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        headers.setInt(CONTENT_LENGTH, resp.content().readableBytes());
        headers.set(ACCESS_CONTROL_ALLOW_ORIGIN, channel.attr(NettyAttr.ORIGIN).get());
        headers.set(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        headers.set(ACCESS_CONTROL_ALLOW_METHODS, "GET,HEAD,PUT,PATCH,POST,DELETE");
        headers.set(ACCESS_CONTROL_ALLOW_HEADERS, "origin, content-type, accept");
    }
}
