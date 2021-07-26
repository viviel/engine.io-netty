package com.github.viviel.engine.io.netty;

import com.github.viviel.engine.io.EngineIOContext;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class EngineIONettyServer {

    private final EngineIOContext ioCtx;

    private EventLoopGroup group;

    public EngineIONettyServer(EngineIOContext ioCtx) {
        this.ioCtx = ioCtx;
    }

    public void start() {
        this.group = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(group)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new EngineIOChannelInitializer(ioCtx));
        b.bind(8888);
    }

    private void shutdown() {
        group.shutdownGracefully();
    }
}
