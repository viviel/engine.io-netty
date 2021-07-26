package com.github.viviel.engine.io.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandlerRegister {

    private final Logger log = LoggerFactory.getLogger(HandlerRegister.class);

    private OpenHandler openHandler;

    private CloseHandler closeHandler;

    private PingHandler pingHandler;

    private UpgradeHandler upgradeHandler;

    private MessageHandler messageHandler;

    public OpenHandler getOpenHandler() {
        if (openHandler == null) {
            return c -> log.debug("open client: {}", c.toString());
        }
        return openHandler;
    }

    public void setOpenHandler(OpenHandler openHandler) {
        this.openHandler = openHandler;
    }

    public CloseHandler getCloseHandler() {
        if (this.closeHandler == null) {
            return client -> log.debug("close channel: {}", client.toString());
        }
        return closeHandler;
    }

    public void setCloseHandler(CloseHandler closeHandler) {
        this.closeHandler = closeHandler;
    }

    public PingHandler getPingHandler() {
        return pingHandler;
    }

    public void setPingHandler(PingHandler pingHandler) {
        this.pingHandler = pingHandler;
    }

    public UpgradeHandler getUpgradeHandler() {
        return upgradeHandler;
    }

    public void setUpgradeHandler(UpgradeHandler upgradeHandler) {
        this.upgradeHandler = upgradeHandler;
    }

    public MessageHandler getMessageHandler() {
        if (this.messageHandler == null) {
            return (channel, msg) -> log.debug("on message. channel: {}, msg: {}", channel.toString(), msg);
        }
        return messageHandler;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }
}
