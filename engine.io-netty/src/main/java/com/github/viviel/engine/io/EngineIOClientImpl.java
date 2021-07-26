package com.github.viviel.engine.io;

import com.github.viviel.engine.io.protocol.Packet;
import com.github.viviel.engine.io.protocol.PacketType;
import com.github.viviel.engine.io.timer.TimerKey;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

public class EngineIOClientImpl implements EngineIOClient {

    public static final int INIT = 0;
    public static final int OPEN = 1;
    public static final int CLOSE = 2;

    private transient int status;

    private transient boolean poll;

    private final String sid;

    private final Queue<Packet> packets;

    private final EngineIOContext ioCtx;

    private TransportType currentTransport;

    private ChannelHandlerContext pollingCtx;

    private ChannelHandlerContext websocketCtx;

    public EngineIOClientImpl(String sid, EngineIOContext ioCtx) {
        this.status = INIT;
        this.poll = false;
        this.sid = sid;
        this.ioCtx = ioCtx;
        packets = new ConcurrentLinkedQueue<>();
    }

    @Override
    public String toString() {
        return "EngineIOClientImpl{" +
               "status=" + status +
               ", sid='" + sid + '\'' +
               ", currentTransport=" + currentTransport +
               '}';
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public synchronized void poll() {
        poll = true;
    }

    public synchronized void unPoll() {
        poll = false;
    }

    public String getSid() {
        return sid;
    }

    public TransportType getCurrentTransport() {
        return currentTransport;
    }

    public synchronized void setCurrentTransport(TransportType transport) {
        this.currentTransport = transport;
    }

    public synchronized void setPollingCtx(ChannelHandlerContext pollingCtx) {
        ChannelHandlerContext pc = this.pollingCtx;
        if (pc != null) {
            pc.close().syncUninterruptibly();
        }
        this.pollingCtx = pollingCtx;
    }

    public ChannelHandlerContext getWebsocketCtx() {
        return websocketCtx;
    }

    public void setWebsocketCtx(ChannelHandlerContext websocketCtx) {
        this.websocketCtx = websocketCtx;
    }

    public synchronized List<Packet> pollPackets() {
        List<Packet> l = new ArrayList<>();
        int size = packets.size();
        for (int i = 0; i < size; i++) {
            l.add(packets.poll());
        }
        return l;
    }

    public void ping() {
        TimerKey key = TimerKey.of(TimerKey.Type.PING, this);
        int interval = ioCtx.ioOpt().getPingInterval();
        ioCtx.timer().schedule(key, interval, () -> {
            send(new Packet(PacketType.PING));
            pingTimeout();
        });
    }

    private void cancelPing() {
        TimerKey key = TimerKey.of(TimerKey.Type.PING, this);
        ioCtx.timer().cancel(key);
    }

    private void pingTimeout() {
        TimerKey key = TimerKey.of(TimerKey.Type.PING_TIMEOUT, this);
        int interval = ioCtx.ioOpt().getPingTimeout();
        ioCtx.timer().schedule(key, interval, this::close);
    }

    public void cancelPingTimeout() {
        TimerKey key = TimerKey.of(TimerKey.Type.PING_TIMEOUT, this);
        ioCtx.timer().cancel(key);
    }

    @Override
    public void send(Packet packet) {
        List<Packet> packets = Collections.singletonList(packet);
        send(packets);
    }

    @Override
    public void send(String msg) {
        Packet packet = new Packet(PacketType.MESSAGE);
        packet.setData(msg);
        send(packet);
    }

    private void send(List<Packet> packets) {
        if (status != OPEN) {
            return;
        }
        this.packets.addAll(packets);
        flush();
    }

    public void flush() {
        if (currentTransport.equals(TransportType.POLLING)) {
            flushPolling();
        } else {
            flushWebsocket();
        }
    }

    public synchronized void flushPolling() {
        if (!poll) {
            return;
        }
        List<Packet> p = pollPackets();
        if (p.size() == 0) {
            return;
        }
        unPoll();
        ioCtx.pollingProcessor().send(pollingCtx, p);
    }

    public void flushWebsocket() {
        List<Packet> p = pollPackets();
        if (p.size() == 0) {
            return;
        }
        for (Packet e : p) {
            ioCtx.websocketProcessor().send(websocketCtx, e);
        }
    }

    @Override
    public void close() {
        ioCtx.handlerRegister().getCloseHandler().close(this);
        status = CLOSE;
        cancelPing();
        cancelPingTimeout();
        closeCtx();
    }

    private void closeCtx() {
        ChannelHandlerContext pc = this.pollingCtx;
        if (pc != null) {
            pc.close();
        }
        ChannelHandlerContext wc = this.websocketCtx;
        if (wc != null) {
            wc.close();
        }
    }

    public Executor getExecutor() {
        ChannelHandlerContext pc = this.pollingCtx;
        if (pc != null) {
            return pc.executor();
        }
        ChannelHandlerContext wc = this.websocketCtx;
        if (wc != null) {
            return wc.executor();
        }
        return ioCtx.executor();
    }
}
