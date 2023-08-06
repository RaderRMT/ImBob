package fr.rader.imbob.packets;

import fr.rader.imbob.packets.data.DataBlock;
import fr.rader.imbob.protocol.Protocol;
import fr.rader.imbob.types.VarInt;

public class Packet {

    private final Protocol protocolVersion;
    private final VarInt packetId;

    private final DataBlock data;

    private String packetName;

    public Packet(final Protocol protocolVersion, final VarInt packetId) {
        this.protocolVersion = protocolVersion;
        this.packetId = packetId;

        this.data = new DataBlock();
    }

    public Protocol getProtocol() {
        return this.protocolVersion;
    }

    public VarInt getPacketId() {
        return this.packetId;
    }

    public String getPacketName() {
        return this.packetName;
    }

    public void setPacketName(final String packetName) {
        this.packetName = packetName;
    }

    public void add(final String name, final Object value) {
        this.data.add(name, value);
    }

    public void update(final String name, final Object value) {
        this.data.update(name, value);
    }

    public <T> T get(final String name, final Class<T> clazz) {
        return this.data.get(name, clazz);
    }

    public boolean isEmpty() {
        return this.data.isEmpty();
    }

    public Packet cloneEmpty() {
        Packet clonedPacket = new Packet(this.protocolVersion, this.packetId);
        clonedPacket.setPacketName(this.packetName);

        return clonedPacket;
    }

    @Override
    public String toString() {
        return "NewPacket{" +
                "protocolVersion=" + protocolVersion +
                ", packetId=" + packetId +
                ", data=" + data +
                ", packetName='" + packetName + '\'' +
                '}';
    }
}
