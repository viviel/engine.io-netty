package com.github.viviel.engine.io.netty;

import com.github.viviel.engine.io.EngineIOClientImpl;
import com.github.viviel.engine.io.EngineIOContext;
import com.github.viviel.engine.io.RequestDispatcher;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;

public class EngineIOChannelHandler extends SimpleChannelInboundHandler<Object> {

    private final RequestDispatcher dispatcher;

    public EngineIOChannelHandler(EngineIOContext ioCtx) {
        this.dispatcher = ioCtx.requestDispatcher();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        dispatcher.dispatch(ctx, msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Attribute<EngineIOClientImpl> attr = ctx.channel().attr(NettyAttr.CLIENT);
        EngineIOClientImpl client = attr.get();
        if (client != null) {
            client.close();
        }
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
