package com.github.viviel.engine.io.protocol;

import java.util.Arrays;
import java.util.Objects;

public enum PacketType {

    OPEN(0),
    CLOSE(1),
    PING(2),
    PONG(3),
    MESSAGE(4),
    UPGRADE(5),
    NOOP(6),
    ERROR(-1),
    ;

    private final int type;

    PacketType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static PacketType valueOf(int type) {
        return Arrays.stream(values())
                .filter(e -> Objects.equals(e.type, type))
                .findFirst()
                .orElse(ERROR);
    }
}
