package com.github.viviel.engine.io.protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Handles parsing of engine.io packets and payloads.
 */
public final class Parser {

    @SuppressWarnings("WeakerAccess")
    public static final int PROTOCOL = 4;
    private static final String SEPARATOR = "\u001E";   // (char) 30
    private static final Packet ERROR_PACKET = new Packet(PacketType.ERROR, "parser error");

    private Parser() {
    }

    /**
     * Encode a packet for transfer over transport.
     *
     * @param packet The packet to encode.
     */
    public static <R> R encodePacket(Packet packet, Function<String, R> f) {
        String encoded = String.valueOf(packet.getType().getType());
        if (null != packet.getData()) {
            encoded += packet.getData();
        }
        return f.apply(encoded);
    }

    /**
     * Encode an array of packets into a payload for transfer over transport.
     *
     * @param packets Array of packets to encode.
     */
    public static <R> R encodePayload(List<Packet> packets, Function<String, R> f) {
        final String[] encodedPackets = new String[packets.size()];
        for (int i = 0; i < encodedPackets.length; i++) {
            final Packet packet = packets.get(i);
            final int packetIdx = i;
            encodePacket(packet, data -> encodedPackets[packetIdx] = data);
        }
        return f.apply(String.join(SEPARATOR, encodedPackets));
    }

    /**
     * Decode a packet received from transport.
     *
     * @param data Data received from transport.
     * @return Packet decoded from data.
     */
    public static Packet decodePacket(String data) {
        if (data == null) {
            return ERROR_PACKET;
        }
        int type = Integer.parseInt(String.valueOf(data.charAt(0)));
        Packet packet = new Packet(PacketType.valueOf(type));
        packet.setData(data.substring(1));
        return packet;
    }

    /**
     * Decode payload received from transport.
     *
     * @param data Data received from transport.
     */
    public static void decodePayload(String data, Function<Packet, Boolean> f) {
        assert f != null;
        final ArrayList<Packet> packets = new ArrayList<>();
        final String[] encodedPackets = data.split(SEPARATOR);
        for (String encodedPacket : encodedPackets) {
            final Packet packet = decodePacket(encodedPacket);
            packets.add(packet);
            if (packet.getType().equals(PacketType.ERROR)) {
                break;
            }
        }
        for (Packet p : packets) {
            if (!f.apply(p)) {
                break;
            }
        }
    }
}
