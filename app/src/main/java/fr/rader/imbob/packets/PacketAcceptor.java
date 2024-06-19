package fr.rader.imbob.packets;

import fr.rader.imbob.protocol.Protocol;

public class PacketAcceptor {

    private final PacketMetaData packetToAccept;
    private final boolean isConfigurationPacket;

    private Protocol fromVersion;
    private Protocol toVersion;

    private PacketAcceptor(final PacketMetaData packet, final boolean isConfigurationPacket) {
        this.packetToAccept = packet;
        this.isConfigurationPacket = isConfigurationPacket;
    }

    /**
     * Create a new {@link PacketAcceptor} for the given {@link PacketMetaData}
     *
     * @param packet    The packet to accept
     * @return          A new {@link PacketAcceptor} for the {@link PacketMetaData}
     */
    public static PacketAcceptor accept(final PacketMetaData packet) {
        return accept(packet, false);
    }

    /**
     * Create a new {@link PacketAcceptor} for the given {@link PacketMetaData}
     *
     * @param packet    The packet to accept
     * @return          A new {@link PacketAcceptor} for the {@link PacketMetaData}
     */
    public static PacketAcceptor accept(final PacketMetaData packet, final boolean isConfigurationPacket) {
        return new PacketAcceptor(packet, isConfigurationPacket);
    }

    /**
     * Accept the packet starting from the given {@link Protocol} (included)
     *
     * @param fromVersion       The version to accept the packet from
     * @return                  The {@link PacketAcceptor} instance to chain methods
     */
    public PacketAcceptor from(final Protocol fromVersion) {
        this.fromVersion = fromVersion;
        return this;
    }

    /**
     * Accept the packet up to the given {@link Protocol} (included)
     *
     * @param toVersion         The version to accept the packet up to
     * @return                  The {@link PacketAcceptor} instance to chain methods
     */
    public PacketAcceptor to(final Protocol toVersion) {
        this.toVersion = toVersion;
        return this;
    }

    /**
     * Accepts a packet.<br>
     * A packet is accepted if its protocol version is between {@link PacketAcceptor#fromVersion} and {@link PacketAcceptor#toVersion}, and if the packet id is accepted by the {@link PacketMetaData}.<br>
     * If {@link PacketAcceptor#fromVersion} is null, any packet up to {@link PacketAcceptor#toVersion} are accepted.<br>
     * If {@link PacketAcceptor#toVersion} is null, any packet from {@link PacketAcceptor#fromVersion} and later are accepted.<br>
     * If both {@link PacketAcceptor#fromVersion} and {@link PacketAcceptor#toVersion} are null, the packet will be accepted.<br>
     *
     * @param packet    The packet to accept
     * @return          True if the packet is accepted, false otherwise
     */
    public boolean accept(final Packet packet) {
        if (packet.isConfigurationPacket() != this.isConfigurationPacket) {
            return false;
        }

        if (this.toVersion != null) {
            if (this.fromVersion != null) {
                return packet.getProtocol().isAfterInclusive(this.fromVersion) &&
                        packet.getProtocol().isBeforeInclusive(this.toVersion) &&
                        this.packetToAccept.accept(packet);
            } else {
                return packet.getProtocol().isBeforeInclusive(this.toVersion) &&
                        this.packetToAccept.accept(packet);
            }
        } else {
            if (this.fromVersion != null) {
                return packet.getProtocol().isAfterInclusive(this.fromVersion) &&
                        this.packetToAccept.accept(packet);
            }
        }

        return this.packetToAccept.accept(packet);
    }
}
