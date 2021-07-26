package com.github.viviel.engine.io.netty;

import com.github.viviel.engine.io.EngineIOContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

public class EngineIOChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final EngineIOContext ioCtx;

    private final int maxContentLength;

    public EngineIOChannelInitializer(EngineIOContext ioCtx) {
        this(ioCtx, 1024 * 512);
    }

    public EngineIOChannelInitializer(EngineIOContext ioCtx, int maxHttpContentLength) {
        this.ioCtx = ioCtx;
        checkPositiveOrZero(maxHttpContentLength, "maxHttpContentLength");
        this.maxContentLength = maxHttpContentLength;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(maxContentLength));
        pipeline.addLast(new EngineIOChannelHandler(ioCtx));
    }
}
