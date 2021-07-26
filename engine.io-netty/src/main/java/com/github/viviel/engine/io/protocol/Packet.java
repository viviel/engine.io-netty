package com.github.viviel.engine.io.protocol;


public class Packet {

    public static Packet NOOP = new Packet(PacketType.NOOP);

    private PacketType type;
    private String data;

    public Packet(PacketType type) {
        this(type, null);
    }

    public Packet(PacketType type, String data) {
        this.type = type;
        this.data = data;
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "[" + type + "] data: " + data;
    }
}
