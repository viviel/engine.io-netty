package com.github.viviel.engine.io;

import io.netty.channel.ChannelHandlerContext;

public class TransportInfo<T> {

    private ChannelHandlerContext ctx;

    private EngineIOClientImpl client;

    private TransportType type;

    private T data;

    public static class Builder<T> {

        private final TransportInfo<T> info;

        Builder() {
            info = new TransportInfo<>();
        }

        public Builder<T> ctx(ChannelHandlerContext ctx) {
            info.ctx = ctx;
            return this;
        }

        public Builder<T> client(EngineIOClientImpl c) {
            info.client = c;
            return this;
        }

        public Builder<T> type(TransportType type) {
            info.type = type;
            return this;
        }

        public Builder<T> data(T t) {
            info.data = t;
            return this;
        }

        public TransportInfo<T> build() {
            return info;
        }
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private TransportInfo() {
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public EngineIOClientImpl getClient() {
        return client;
    }

    public TransportType getType() {
        return type;
    }

    public T getData() {
        return data;
    }
}
